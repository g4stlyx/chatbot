package com.g4.chatbot.controllers;

import com.g4.chatbot.dto.session.*;
import com.g4.chatbot.services.ChatSessionService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sessions")
@Slf4j
public class ChatSessionController {
    
    @Autowired
    private ChatSessionService chatSessionService;
    
    /**
     * Create a new chat session
     * POST /api/v1/sessions
     */
    @PostMapping
    public ResponseEntity<SessionResponse> createSession(
            @Valid @RequestBody CreateSessionRequest request,
            Authentication authentication) {
        
        Long userId = (Long) authentication.getDetails();
        log.info("User {} creating new session", userId);
        
        SessionResponse response = chatSessionService.createSession(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Get all sessions for the authenticated user with pagination
     * GET /api/v1/sessions?page=0&size=10
     */
    @GetMapping
    public ResponseEntity<SessionListResponse> getUserSessions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        
        Long userId = (Long) authentication.getDetails();
        log.info("User {} fetching sessions (page: {}, size: {})", userId, page, size);
        
        SessionListResponse response = chatSessionService.getUserSessions(userId, page, size);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get all active sessions for the authenticated user
     * GET /api/v1/sessions/active
     */
    @GetMapping("/active")
    public ResponseEntity<List<SessionResponse>> getActiveSessions(
            Authentication authentication) {
        
        Long userId = (Long) authentication.getDetails();
        log.info("User {} fetching active sessions", userId);
        
        List<SessionResponse> response = chatSessionService.getUserActiveSessions(userId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get a specific session by ID
     * GET /api/v1/sessions/{sessionId}
     */
    @GetMapping("/{sessionId}")
    public ResponseEntity<SessionResponse> getSession(
            @PathVariable String sessionId,
            Authentication authentication) {
        
        Long userId = (Long) authentication.getDetails();
        log.info("User {} fetching session {}", userId, sessionId);
        
        SessionResponse response = chatSessionService.getSession(sessionId, userId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Update a session
     * PUT /api/v1/sessions/{sessionId}
     */
    @PutMapping("/{sessionId}")
    public ResponseEntity<SessionResponse> updateSession(
            @PathVariable String sessionId,
            @Valid @RequestBody UpdateSessionRequest request,
            Authentication authentication) {
        
        Long userId = (Long) authentication.getDetails();
        log.info("User {} updating session {}", userId, sessionId);
        
        SessionResponse response = chatSessionService.updateSession(sessionId, userId, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Delete a session (soft delete)
     * DELETE /api/v1/sessions/{sessionId}
     */
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> deleteSession(
            @PathVariable String sessionId,
            Authentication authentication) {
        
        Long userId = (Long) authentication.getDetails();
        log.info("User {} deleting session {}", userId, sessionId);
        
        chatSessionService.deleteSession(sessionId, userId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Archive a session
     * POST /api/v1/sessions/{sessionId}/archive
     */
    @PostMapping("/{sessionId}/archive")
    public ResponseEntity<SessionResponse> archiveSession(
            @PathVariable String sessionId,
            Authentication authentication) {
        
        Long userId = (Long) authentication.getDetails();
        log.info("User {} archiving session {}", userId, sessionId);
        
        SessionResponse response = chatSessionService.archiveSession(sessionId, userId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Pause a session
     * POST /api/v1/sessions/{sessionId}/pause
     */
    @PostMapping("/{sessionId}/pause")
    public ResponseEntity<SessionResponse> pauseSession(
            @PathVariable String sessionId,
            Authentication authentication) {
        
        Long userId = (Long) authentication.getDetails();
        log.info("User {} pausing session {}", userId, sessionId);
        
        SessionResponse response = chatSessionService.pauseSession(sessionId, userId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Activate/Resume a session
     * POST /api/v1/sessions/{sessionId}/activate
     */
    @PostMapping("/{sessionId}/activate")
    public ResponseEntity<SessionResponse> activateSession(
            @PathVariable String sessionId,
            Authentication authentication) {
        
        Long userId = (Long) authentication.getDetails();
        log.info("User {} activating session {}", userId, sessionId);
        
        SessionResponse response = chatSessionService.activateSession(sessionId, userId);
        return ResponseEntity.ok(response);
    }
}
