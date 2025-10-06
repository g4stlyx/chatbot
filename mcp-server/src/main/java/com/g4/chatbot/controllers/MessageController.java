package com.g4.chatbot.controllers;

import com.g4.chatbot.dto.messages.MessageHistoryResponse;
import com.g4.chatbot.dto.messages.MessageResponse;
import com.g4.chatbot.dto.messages.RegenerateRequest;
import com.g4.chatbot.dto.messages.UpdateMessageRequest;
import com.g4.chatbot.services.MessageService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@Slf4j
public class MessageController {
    
    @Autowired
    private MessageService messageService;
    
    /**
     * GET /api/v1/sessions/{sessionId}/messages
     * Get all messages in a session (conversation history)
     */
    @GetMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<MessageHistoryResponse> getSessionMessages(
            @PathVariable String sessionId,
            Authentication authentication) {
        
        Long userId = (Long) authentication.getDetails();
        log.info("User {} requesting message history for session: {}", userId, sessionId);
        
        MessageHistoryResponse response = messageService.getMessageHistory(sessionId, userId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/v1/messages/{messageId}
     * Get a single message by ID
     */
    @GetMapping("/messages/{messageId}")
    public ResponseEntity<MessageResponse> getMessage(
            @PathVariable Long messageId,
            Authentication authentication) {
        
        Long userId = (Long) authentication.getDetails();
        log.info("User {} requesting message: {}", userId, messageId);
        
        MessageResponse response = messageService.getMessage(messageId, userId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * PUT /api/v1/messages/{messageId}
     * Update/edit a message (only USER messages)
     * Optionally regenerate the assistant's response after edit
     */
    @PutMapping("/messages/{messageId}")
    public ResponseEntity<MessageResponse> updateMessage(
            @PathVariable Long messageId,
            @Valid @RequestBody UpdateMessageRequest request,
            Authentication authentication) {
        
        Long userId = (Long) authentication.getDetails();
        log.info("User {} updating message: {}, regenerate: {}", 
                userId, messageId, request.getRegenerateResponse());
        
        MessageResponse response = messageService.updateMessage(messageId, request, userId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * DELETE /api/v1/messages/{messageId}
     * Delete a message
     * If USER message, also deletes corresponding ASSISTANT response
     */
    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable Long messageId,
            Authentication authentication) {
        
        Long userId = (Long) authentication.getDetails();
        log.info("User {} deleting message: {}", userId, messageId);
        
        messageService.deleteMessage(messageId, userId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * POST /api/v1/sessions/{sessionId}/regenerate
     * Regenerate the last assistant response in a session
     */
    @PostMapping("/sessions/{sessionId}/regenerate")
    public ResponseEntity<MessageResponse> regenerateLastResponse(
            @PathVariable String sessionId,
            @RequestBody(required = false) RegenerateRequest request,
            Authentication authentication) {
        
        Long userId = (Long) authentication.getDetails();
        String model = request != null ? request.getModel() : null;
        
        log.info("User {} regenerating last response for session: {}, model: {}", 
                userId, sessionId, model);
        
        MessageResponse response = messageService.regenerateLastResponse(sessionId, userId, model);
        return ResponseEntity.ok(response);
    }
}
