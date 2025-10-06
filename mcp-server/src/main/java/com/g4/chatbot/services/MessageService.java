package com.g4.chatbot.services;

import com.g4.chatbot.dto.messages.MessageHistoryResponse;
import com.g4.chatbot.dto.messages.MessageResponse;
import com.g4.chatbot.dto.messages.UpdateMessageRequest;
import com.g4.chatbot.dto.ollama.OllamaMessage;
import com.g4.chatbot.models.ChatSession;
import com.g4.chatbot.models.Message;
import com.g4.chatbot.repos.ChatSessionRepository;
import com.g4.chatbot.repos.MessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MessageService {
    
    @Autowired
    private MessageRepository messageRepository;
    
    @Autowired
    private ChatSessionRepository chatSessionRepository;
    
    @Autowired
    private OllamaService ollamaService;
    
    /**
     * Get all messages for a session
     */
    public MessageHistoryResponse getMessageHistory(String sessionId, Long userId) {
        log.info("Fetching message history for session: {}, user: {}", sessionId, userId);
        
        // Verify session belongs to user
        ChatSession session = chatSessionRepository.findBySessionIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new RuntimeException("Session not found or access denied"));
        
        List<Message> messages = messageRepository.findBySessionIdOrderByTimestampAsc(sessionId);
        
        List<MessageResponse> messageResponses = messages.stream()
                .map(MessageResponse::from)
                .collect(Collectors.toList());
        
        return MessageHistoryResponse.builder()
                .sessionId(sessionId)
                .totalMessages(messages.size())
                .messages(messageResponses)
                .build();
    }
    
    /**
     * Get a single message by ID
     */
    public MessageResponse getMessage(Long messageId, Long userId) {
        log.info("Fetching message: {} for user: {}", messageId, userId);
        
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        
        // Verify the session belongs to the user
        ChatSession session = chatSessionRepository.findBySessionIdAndUserId(message.getSessionId(), userId)
                .orElseThrow(() -> new RuntimeException("Access denied"));
        
        return MessageResponse.from(message);
    }
    
    /**
     * Update a message (edit content)
     * Only USER messages can be edited
     */
    @Transactional
    public MessageResponse updateMessage(Long messageId, UpdateMessageRequest request, Long userId) {
        log.info("Updating message: {} for user: {}", messageId, userId);
        
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        
        // Verify the session belongs to the user
        ChatSession session = chatSessionRepository.findBySessionIdAndUserId(message.getSessionId(), userId)
                .orElseThrow(() -> new RuntimeException("Access denied"));
        
        // Only allow editing USER messages
        if (message.getRole() != Message.MessageRole.USER) {
            throw new RuntimeException("Only user messages can be edited");
        }
        
        // Update the message content
        message.setContent(request.getContent());
        Message updated = messageRepository.save(message);
        
        log.info("Message {} updated successfully", messageId);
        
        // If regenerateResponse is true, delete the next assistant message and regenerate
        if (Boolean.TRUE.equals(request.getRegenerateResponse())) {
            log.info("Regenerating assistant response after message edit");
            deleteSubsequentMessages(message, userId);
            regenerateFromMessage(message, userId);
        }
        
        return MessageResponse.from(updated);
    }
    
    /**
     * Delete a message
     * If it's a USER message, also delete the corresponding ASSISTANT response
     */
    @Transactional
    public void deleteMessage(Long messageId, Long userId) {
        log.info("Deleting message: {} for user: {}", messageId, userId);
        
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        
        // Verify the session belongs to the user
        ChatSession session = chatSessionRepository.findBySessionIdAndUserId(message.getSessionId(), userId)
                .orElseThrow(() -> new RuntimeException("Access denied"));
        
        // If deleting a USER message, also delete subsequent ASSISTANT response
        if (message.getRole() == Message.MessageRole.USER) {
            List<Message> allMessages = messageRepository.findBySessionIdOrderByTimestampAsc(message.getSessionId());
            
            // Find the next assistant message after this user message
            boolean foundTarget = false;
            for (Message msg : allMessages) {
                if (msg.getId().equals(messageId)) {
                    foundTarget = true;
                    continue;
                }
                if (foundTarget && msg.getRole() == Message.MessageRole.ASSISTANT) {
                    log.info("Also deleting corresponding assistant message: {}", msg.getId());
                    messageRepository.delete(msg);
                    
                    // Update session stats
                    session.setMessageCount(session.getMessageCount() - 2); // User + Assistant
                    session.setTokenUsage(session.getTokenUsage() - 
                            (message.getTokenCount() != null ? message.getTokenCount() : 0) -
                            (msg.getTokenCount() != null ? msg.getTokenCount() : 0));
                    break;
                }
            }
        } else {
            // Deleting assistant message only
            session.setMessageCount(session.getMessageCount() - 1);
            session.setTokenUsage(session.getTokenUsage() - 
                    (message.getTokenCount() != null ? message.getTokenCount() : 0));
        }
        
        messageRepository.delete(message);
        chatSessionRepository.save(session);
        
        log.info("Message {} deleted successfully", messageId);
    }
    
    /**
     * Regenerate the last assistant response in a session
     */
    @Transactional
    public MessageResponse regenerateLastResponse(String sessionId, Long userId, String model) {
        log.info("Regenerating last response for session: {}, user: {}", sessionId, userId);
        
        // Verify session belongs to user
        ChatSession session = chatSessionRepository.findBySessionIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new RuntimeException("Session not found or access denied"));
        
        List<Message> messages = messageRepository.findBySessionIdOrderByTimestampAsc(sessionId);
        
        if (messages.isEmpty()) {
            throw new RuntimeException("No messages found in session");
        }
        
        // Find the last assistant message
        Message lastAssistantMessage = null;
        Message lastUserMessage = null;
        
        for (int i = messages.size() - 1; i >= 0; i--) {
            Message msg = messages.get(i);
            if (msg.getRole() == Message.MessageRole.ASSISTANT && lastAssistantMessage == null) {
                lastAssistantMessage = msg;
            } else if (msg.getRole() == Message.MessageRole.USER && lastUserMessage == null) {
                lastUserMessage = msg;
            }
            
            if (lastAssistantMessage != null && lastUserMessage != null) {
                break;
            }
        }
        
        if (lastAssistantMessage == null) {
            throw new RuntimeException("No assistant message found to regenerate");
        }
        
        // Delete the last assistant message
        messageRepository.delete(lastAssistantMessage);
        session.setMessageCount(session.getMessageCount() - 1);
        session.setTokenUsage(session.getTokenUsage() - 
                (lastAssistantMessage.getTokenCount() != null ? lastAssistantMessage.getTokenCount() : 0));
        
        // Rebuild history without the deleted message
        List<Message> historyForRegeneration = messageRepository.findBySessionIdOrderByTimestampAsc(sessionId);
        
        // Build Ollama messages from history
        List<OllamaMessage> ollamaMessages = new ArrayList<>(ollamaService.buildMessageHistory(historyForRegeneration));
        
        // Use the specified model or session's model or default
        String modelToUse = model != null ? model : 
                           (session.getModel() != null ? session.getModel() : "llama3");
        
        log.info("Regenerating with model: {}, history size: {}", modelToUse, ollamaMessages.size());
        
        // Call Ollama to generate new response
        String assistantResponse = ollamaService.chat(modelToUse, ollamaMessages);
        
        // Save the new assistant message
        Message newAssistantMessage = new Message();
        newAssistantMessage.setSessionId(sessionId);
        newAssistantMessage.setRole(Message.MessageRole.ASSISTANT);
        newAssistantMessage.setContent(assistantResponse);
        newAssistantMessage.setModel(modelToUse);
        newAssistantMessage.setTokenCount(assistantResponse.length() / 4); // Rough estimate
        newAssistantMessage.setTimestamp(LocalDateTime.now());
        
        Message saved = messageRepository.save(newAssistantMessage);
        
        // Update session stats
        session.setMessageCount(session.getMessageCount() + 1);
        session.setTokenUsage(session.getTokenUsage() + newAssistantMessage.getTokenCount());
        session.setUpdatedAt(LocalDateTime.now());
        chatSessionRepository.save(session);
        
        log.info("Response regenerated successfully, new message ID: {}", saved.getId());
        
        return MessageResponse.from(saved);
    }
    
    /**
     * Regenerate from a specific message (used after editing)
     */
    @Transactional
    private void regenerateFromMessage(Message userMessage, Long userId) {
        String sessionId = userMessage.getSessionId();
        
        // Get all messages up to this point
        List<Message> allMessages = messageRepository.findBySessionIdOrderByTimestampAsc(sessionId);
        List<Message> messagesUpToThis = new ArrayList<>();
        
        for (Message msg : allMessages) {
            messagesUpToThis.add(msg);
            if (msg.getId().equals(userMessage.getId())) {
                break;
            }
        }
        
        // Build Ollama messages
        List<OllamaMessage> ollamaMessages = new ArrayList<>(ollamaService.buildMessageHistory(messagesUpToThis));
        
        // Get session info
        ChatSession session = chatSessionRepository.findBySessionIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        
        String modelToUse = session.getModel() != null ? session.getModel() : "llama3";
        
        // Generate new response
        String assistantResponse = ollamaService.chat(modelToUse, ollamaMessages);
        
        // Save new assistant message
        Message newAssistantMessage = new Message();
        newAssistantMessage.setSessionId(sessionId);
        newAssistantMessage.setRole(Message.MessageRole.ASSISTANT);
        newAssistantMessage.setContent(assistantResponse);
        newAssistantMessage.setModel(modelToUse);
        newAssistantMessage.setTokenCount(assistantResponse.length() / 4);
        newAssistantMessage.setTimestamp(LocalDateTime.now());
        
        messageRepository.save(newAssistantMessage);
        
        // Update session stats
        session.setMessageCount(session.getMessageCount() + 1);
        session.setTokenUsage(session.getTokenUsage() + newAssistantMessage.getTokenCount());
        session.setUpdatedAt(LocalDateTime.now());
        chatSessionRepository.save(session);
        
        log.info("Generated new response after message edit");
    }
    
    /**
     * Delete all messages after a specific message (used before regeneration)
     */
    @Transactional
    private void deleteSubsequentMessages(Message fromMessage, Long userId) {
        List<Message> allMessages = messageRepository.findBySessionIdOrderByTimestampAsc(fromMessage.getSessionId());
        
        boolean foundTarget = false;
        List<Message> toDelete = new ArrayList<>();
        
        for (Message msg : allMessages) {
            if (msg.getId().equals(fromMessage.getId())) {
                foundTarget = true;
                continue;
            }
            if (foundTarget) {
                toDelete.add(msg);
            }
        }
        
        if (!toDelete.isEmpty()) {
            log.info("Deleting {} subsequent messages before regeneration", toDelete.size());
            
            ChatSession session = chatSessionRepository.findBySessionIdAndUserId(
                    fromMessage.getSessionId(), userId)
                    .orElseThrow(() -> new RuntimeException("Session not found"));
            
            int totalTokens = 0;
            for (Message msg : toDelete) {
                totalTokens += (msg.getTokenCount() != null ? msg.getTokenCount() : 0);
                messageRepository.delete(msg);
            }
            
            session.setMessageCount(session.getMessageCount() - toDelete.size());
            session.setTokenUsage(session.getTokenUsage() - totalTokens);
            chatSessionRepository.save(session);
        }
    }
}
