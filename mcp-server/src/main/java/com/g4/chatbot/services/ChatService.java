package com.g4.chatbot.services;

import com.g4.chatbot.dto.chat.ChatRequest;
import com.g4.chatbot.dto.chat.ChatResponse;
import com.g4.chatbot.dto.ollama.OllamaMessage;
import com.g4.chatbot.dto.session.CreateSessionRequest;
import com.g4.chatbot.dto.session.SessionResponse;
import com.g4.chatbot.exception.BadRequestException;
import com.g4.chatbot.exception.ResourceNotFoundException;
import com.g4.chatbot.models.ChatSession;
import com.g4.chatbot.models.Message;
import com.g4.chatbot.repos.ChatSessionRepository;
import com.g4.chatbot.repos.MessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Slf4j
public class ChatService {
    
    @Autowired
    private ChatSessionService chatSessionService;
    
    @Autowired
    private OllamaService ollamaService;
    
    @Autowired
    private ChatSessionRepository chatSessionRepository;
    
    @Autowired
    private MessageRepository messageRepository;
    
    /**
     * Handle chat with streaming response (Server-Sent Events)
     */
    public SseEmitter chatWithStreaming(Long userId, ChatRequest request) {
        log.info("Starting streaming chat for user: {}", userId);
        
        SseEmitter emitter = new SseEmitter(300000L); // 5 minute timeout
        
        // Run in separate thread to avoid blocking
        new Thread(() -> {
            try {
                // 1. Get or create session
                String sessionId = getOrCreateSession(userId, request);
                log.info("Using session: {} (requested: {})", sessionId, request.getSessionId());
                
                // 2. Get conversation history BEFORE saving new message
                List<Message> history = messageRepository.findBySessionIdOrderByTimestampAsc(sessionId);
                log.debug("Fetched {} messages from history for session {}", history.size(), sessionId);
                
                // Log what we fetched from DB
                for (int i = 0; i < history.size(); i++) {
                    Message msg = history.get(i);
                    log.info("DB Message {}: role={}, length={}, id={}", 
                        i+1, msg.getRole(), msg.getContent().length(), msg.getId());
                }
                
                List<OllamaMessage> ollamaMessages = new java.util.ArrayList<>(ollamaService.buildMessageHistory(history));
                log.debug("Built {} Ollama messages from history", ollamaMessages.size());
                
                // 3. Add current user message to the conversation
                ollamaMessages.add(OllamaMessage.builder()
                        .role("user")
                        .content(request.getMessage())
                        .build());
                
                log.info("Sending {} total messages to Ollama (including current message)", ollamaMessages.size());
                for (int i = 0; i < ollamaMessages.size(); i++) {
                    OllamaMessage msg = ollamaMessages.get(i);
                    log.info("Message {}: role={}, length={}", i+1, msg.getRole(), msg.getContent().length());
                    // Log first 200 chars of each message
                    String preview = msg.getContent().length() > 200 
                        ? msg.getContent().substring(0, 200) + "..." 
                        : msg.getContent();
                    log.info("  Content preview: {}", preview);
                }
                
                // 4. Save user message to DB
                Message userMessage = saveUserMessage(sessionId, request.getMessage(), request.getModel());
                
                // 5. Send session info first
                emitter.send(SseEmitter.event()
                        .name("session")
                        .data("{\"sessionId\":\"" + sessionId + "\",\"userMessageId\":" + userMessage.getId() + "}"));
                
                // 6. Stream LLM response
                StringBuilder fullResponse = new StringBuilder();
                AtomicReference<Long> assistantMessageId = new AtomicReference<>();
                
                ollamaService.chatStream(
                        request.getModel() != null ? request.getModel() : 
                        chatSessionRepository.findById(sessionId).map(ChatSession::getModel).orElse("llama3"),
                        ollamaMessages
                ).subscribe(
                        chunk -> {
                            try {
                                // Parse the JSON chunk to extract just the content
                                String content = parseChunkContent(chunk);
                                if (content != null && !content.isEmpty()) {
                                    fullResponse.append(content);
                                    emitter.send(SseEmitter.event()
                                            .name("message")
                                            .data(content));
                                }
                            } catch (IOException e) {
                                log.error("Error sending SSE chunk", e);
                                emitter.completeWithError(e);
                            }
                        },
                        error -> {
                            log.error("Error in streaming", error);
                            emitter.completeWithError(error);
                        },
                        () -> {
                            try {
                                // 7. Save assistant message
                                Message assistantMessage = saveAssistantMessage(
                                        sessionId, 
                                        fullResponse.toString(), 
                                        request.getModel()
                                );
                                assistantMessageId.set(assistantMessage.getId());
                                
                                // 8. Update session stats
                                updateSessionStats(sessionId);
                                
                                // 9. Send completion event
                                emitter.send(SseEmitter.event()
                                        .name("done")
                                        .data("{\"assistantMessageId\":" + assistantMessage.getId() + "}"));
                                
                                emitter.complete();
                                log.info("Streaming chat completed for user: {}", userId);
                            } catch (Exception e) {
                                log.error("Error completing streaming", e);
                                emitter.completeWithError(e);
                            }
                        }
                );
                
            } catch (Exception e) {
                log.error("Error in chat streaming", e);
                emitter.completeWithError(e);
            }
        }).start();
        
        return emitter;
    }
    
    /**
     * Helper class to hold chat context between transaction boundaries
     */
    private static class ChatContext {
        final String sessionId;
        final boolean isNewSession;
        final Message userMessage;
        final List<OllamaMessage> ollamaMessages;
        final String model;
        
        ChatContext(String sessionId, boolean isNewSession, Message userMessage, 
                   List<OllamaMessage> ollamaMessages, String model) {
            this.sessionId = sessionId;
            this.isNewSession = isNewSession;
            this.userMessage = userMessage;
            this.ollamaMessages = ollamaMessages;
            this.model = model;
        }
    }
    
    /**
     * Handle chat without streaming (wait for complete response)
     * Split into separate transactions to avoid holding DB connection during Ollama call
     */
    public ChatResponse chat(Long userId, ChatRequest request) {
        log.info("Processing non-streaming chat for user: {}", userId);
        
        try {
            // Step 1: Prepare chat context in a transaction (fast DB operations)
            ChatContext context = prepareChatContextInTransaction(userId, request);
            
            // Step 2: Call Ollama (long-running) - NO DB transaction held
            String assistantResponse = ollamaService.chat(context.model, context.ollamaMessages);
            
            // Step 3: Save result and update stats in a transaction (fast DB operations)
            ChatResponse response = saveChatResponseInTransaction(
                context.sessionId, context.userMessage, assistantResponse, 
                context.model, context.isNewSession
            );
            
            return response;
                    
        } catch (Exception e) {
            log.error("Error processing chat", e);
            throw new RuntimeException("Failed to process chat: " + e.getMessage());
        }
    }
    
    /**
     * Prepare chat context within a transaction
     */
    @Transactional
    private ChatContext prepareChatContextInTransaction(Long userId, ChatRequest request) {
        // 1. Get or create session
        String sessionId = getOrCreateSession(userId, request);
        boolean isNewSession = request.getSessionId() == null;
        log.info("Using session: {} (isNew: {}, requested: {})", 
            sessionId, isNewSession, request.getSessionId());
        
        // 2. Get conversation history BEFORE saving new message
        List<Message> history = messageRepository.findBySessionIdOrderByTimestampAsc(sessionId);
        log.debug("Fetched {} messages from history for session {}", history.size(), sessionId);
        
        // Log what we fetched from DB
        for (int i = 0; i < history.size(); i++) {
            Message msg = history.get(i);
            log.info("DB Message {}: role={}, length={}, id={}", 
                i+1, msg.getRole(), msg.getContent().length(), msg.getId());
        }
        
        List<OllamaMessage> ollamaMessages = new java.util.ArrayList<>(ollamaService.buildMessageHistory(history));
        log.debug("Built {} Ollama messages from history", ollamaMessages.size());
        
        // 3. Add current user message to the conversation
        ollamaMessages.add(OllamaMessage.builder()
                .role("user")
                .content(request.getMessage())
                .build());
        
        log.info("Sending {} total messages to Ollama (including current message)", ollamaMessages.size());
        for (int i = 0; i < ollamaMessages.size(); i++) {
            OllamaMessage msg = ollamaMessages.get(i);
            log.info("Message {}: role={}, length={}", i+1, msg.getRole(), msg.getContent().length());
            // Log first 200 chars of each message
            String preview = msg.getContent().length() > 200 
                ? msg.getContent().substring(0, 200) + "..." 
                : msg.getContent();
            log.info("  Content preview: {}", preview);
        }
        
        // 4. Save user message to DB
        Message userMessage = saveUserMessage(sessionId, request.getMessage(), request.getModel());
        
        // 5. Determine model to use
        String model = request.getModel() != null ? request.getModel() : 
                      chatSessionRepository.findById(sessionId).map(ChatSession::getModel).orElse("llama3");
        
        return new ChatContext(sessionId, isNewSession, userMessage, ollamaMessages, model);
    }
    
    /**
     * Save chat response within a transaction
     */
    @Transactional
    private ChatResponse saveChatResponseInTransaction(String sessionId, Message userMessage, 
                                                       String assistantResponse, String model, 
                                                       boolean isNewSession) {
        // 6. Save assistant message
        Message assistantMessage = saveAssistantMessage(sessionId, assistantResponse, model);
        
        // 7. Update session stats
        updateSessionStats(sessionId);
        
        // 8. Build response
        return ChatResponse.builder()
                .sessionId(sessionId)
                .userMessageId(userMessage.getId())
                .assistantMessageId(assistantMessage.getId())
                .userMessage(userMessage.getContent())
                .assistantMessage(assistantMessage.getContent())
                .model(model)
                .tokenCount(assistantMessage.getTokenCount())
                .timestamp(LocalDateTime.now())
                .isNewSession(isNewSession)
                .build();
    }
    
    /**
     * Get existing session or create new one
     */
    private String getOrCreateSession(Long userId, ChatRequest request) {
        if (request.getSessionId() != null && !request.getSessionId().isEmpty()) {
            // Verify session exists and belongs to user
            ChatSession session = chatSessionRepository.findById(request.getSessionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + request.getSessionId()));
            
            if (!session.getUserId().equals(userId)) {
                throw new BadRequestException("Session does not belong to user");
            }
            
            return session.getSessionId();
        } else {
            // Create new session
            CreateSessionRequest createRequest = new CreateSessionRequest();
            createRequest.setTitle(request.getSessionTitle() != null ? request.getSessionTitle() : "New Chat");
            createRequest.setModel(request.getModel() != null ? request.getModel() : "llama3");
            createRequest.setIsPublic(false);
            
            SessionResponse newSession = chatSessionService.createSession(userId, createRequest);
            log.info("Auto-created new session: {}", newSession.getSessionId());
            return newSession.getSessionId();
        }
    }
    
    /**
     * Save user message to database
     */
    private Message saveUserMessage(String sessionId, String content, String model) {
        log.debug("Saving user message: {} chars", content.length());
        Message message = new Message();
        message.setSessionId(sessionId);
        message.setRole(Message.MessageRole.USER);
        message.setContent(content);
        message.setModel(model);
        message.setTokenCount(estimateTokenCount(content));
        message.setTimestamp(LocalDateTime.now());
        
        Message saved = messageRepository.save(message);
        log.debug("Saved user message: {} with content length: {}", saved.getId(), saved.getContent().length());
        return saved;
    }
    
    /**
     * Save assistant message to database
     */
    private Message saveAssistantMessage(String sessionId, String content, String model) {
        log.info("Saving assistant message: {} chars", content.length());
        log.debug("Assistant content preview: {}", 
            content.length() > 200 ? content.substring(0, 200) + "..." : content);
        
        Message message = new Message();
        message.setSessionId(sessionId);
        message.setRole(Message.MessageRole.ASSISTANT);
        message.setContent(content);
        message.setModel(model);
        message.setTokenCount(estimateTokenCount(content));
        message.setTimestamp(LocalDateTime.now());
        
        Message saved = messageRepository.save(message);
        log.info("Saved assistant message: {} with content length: {}", saved.getId(), saved.getContent().length());
        return saved;
    }
    
    /**
     * Update session statistics
     */
    private void updateSessionStats(String sessionId) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));
        
        long messageCount = messageRepository.countBySessionId(sessionId);
        Long totalTokens = messageRepository.sumTokenCountBySessionId(sessionId);
        
        session.setMessageCount((int) messageCount);
        session.setTokenUsage(totalTokens != null ? totalTokens : 0L);
        session.setLastAccessedAt(LocalDateTime.now());
        
        chatSessionRepository.save(session);
        log.debug("Updated session stats - messages: {}, tokens: {}", messageCount, totalTokens);
    }
    
    /**
     * Estimate token count (rough approximation: 1 token ≈ 4 characters)
     */
    private Integer estimateTokenCount(String text) {
        if (text == null) return 0;
        return text.length() / 4;
    }
    
    /**
     * Parse content from Ollama streaming chunk
     */
    private String parseChunkContent(String chunk) {
        try {
            // Ollama returns JSON: {"model":"llama3","message":{"role":"assistant","content":"text"},"done":false}
            if (chunk.contains("\"content\":\"")) {
                int start = chunk.indexOf("\"content\":\"") + 11;
                int end = chunk.indexOf("\"", start);
                if (end > start) {
                    return chunk.substring(start, end)
                            .replace("\\n", "\n")
                            .replace("\\\"", "\"")
                            .replace("\\\\", "\\");
                }
            }
            return "";
        } catch (Exception e) {
            log.warn("Failed to parse chunk: {}", chunk);
            return "";
        }
    }
}
