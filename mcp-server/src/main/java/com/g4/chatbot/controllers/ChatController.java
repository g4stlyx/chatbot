package com.g4.chatbot.controllers;

import com.g4.chatbot.dto.chat.ChatRequest;
import com.g4.chatbot.dto.chat.ChatResponse;
import com.g4.chatbot.services.ChatService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/chat")
@Slf4j
public class ChatController {
    
    @Autowired
    private ChatService chatService;
    
    /**
     * Chat with streaming response (Server-Sent Events)
     * POST /api/v1/chat/stream
     * 
     * This endpoint returns a stream of chunks as the LLM generates the response.
     * Perfect for real-time chat experience where users see text appearing character by character.
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(
            @Valid @RequestBody ChatRequest request,
            Authentication authentication) {
        
        Long userId = (Long) authentication.getDetails();
        log.info("User {} initiating streaming chat", userId);
        
        return chatService.chatWithStreaming(userId, request);
    }
    
    /**
     * Chat without streaming (wait for complete response)
     * POST /api/v1/chat
     * 
     * This endpoint waits for the complete LLM response before returning.
     * Simpler to use but less interactive than streaming.
     */
    @PostMapping
    public ResponseEntity<ChatResponse> chat(
            @Valid @RequestBody ChatRequest request,
            Authentication authentication) {
        
        Long userId = (Long) authentication.getDetails();
        log.info("User {} sending chat message", userId);
        
        ChatResponse response = chatService.chat(userId, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    
    /**
     * Alternative: Chat for specific session (streaming)
     * POST /api/v1/sessions/{sessionId}/chat/stream
     */
    @PostMapping(value = "/sessions/{sessionId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStreamForSession(
            @PathVariable String sessionId,
            @Valid @RequestBody ChatRequest request,
            Authentication authentication) {
        
        Long userId = (Long) authentication.getDetails();
        log.info("User {} initiating streaming chat for session {}", userId, sessionId);
        
        // Override sessionId from path
        request.setSessionId(sessionId);
        
        return chatService.chatWithStreaming(userId, request);
    }
    
    /**
     * Alternative: Chat for specific session (non-streaming)
     * POST /api/v1/sessions/{sessionId}/chat
     */
    @PostMapping("/sessions/{sessionId}")
    public ResponseEntity<ChatResponse> chatForSession(
            @PathVariable String sessionId,
            @Valid @RequestBody ChatRequest request,
            Authentication authentication) {
        
        Long userId = (Long) authentication.getDetails();
        log.info("User {} sending chat message to session {}", userId, sessionId);
        
        // Override sessionId from path
        request.setSessionId(sessionId);
        
        ChatResponse response = chatService.chat(userId, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
