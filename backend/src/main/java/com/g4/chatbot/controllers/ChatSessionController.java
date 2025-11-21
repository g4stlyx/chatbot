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
     * Get all public sessions (no authentication required)
     * GET /api/v1/sessions/public?page=0&size=10
     */
    @GetMapping("/public")
    public ResponseEntity<SessionListResponse> getPublicSessions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("Fetching public sessions (page: {}, size: {})", page, size);
        
        SessionListResponse response = chatSessionService.getPublicSessions(page, size);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get a specific public session (no authentication required)
     * GET /api/v1/sessions/public/{sessionId}
     */
    @GetMapping("/public/{sessionId}")
    public ResponseEntity<SessionResponse> getPublicSession(
            @PathVariable String sessionId) {
        
        log.info("Fetching public session {}", sessionId);
        
        SessionResponse response = chatSessionService.getPublicSession(sessionId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Copy a public session to user's own sessions
     * POST /api/v1/sessions/public/{sessionId}/copy
     */
    @PostMapping("/public/{sessionId}/copy")
    public ResponseEntity<SessionResponse> copyPublicSession(
            @PathVariable String sessionId,
            @Valid @RequestBody(required = false) CopySessionRequest request,
            Authentication authentication) {
        
        Long userId = (Long) authentication.getDetails();
        log.info("User {} copying public session {}", userId, sessionId);
        
        String newTitle = request != null ? request.getNewTitle() : null;
        SessionResponse response = chatSessionService.copyPublicSession(sessionId, userId, newTitle);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Search sessions by title
     * GET /api/v1/sessions/search?q={searchTerm}&page=0&size=10
     */
    @GetMapping("/search")
    public ResponseEntity<SessionListResponse> searchSessions(
            @RequestParam("q") String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        
        Long userId = (Long) authentication.getDetails();
        log.info("User {} searching sessions with term: {}", userId, searchTerm);
        
        SessionListResponse response = chatSessionService.searchSessionsByTitle(userId, searchTerm, page, size);
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
    
    /**
     * Toggle session visibility (public/private)
     * PATCH /api/v1/sessions/{sessionId}/visibility
     */
    @PatchMapping("/{sessionId}/visibility")
    public ResponseEntity<SessionResponse> toggleSessionVisibility(
            @PathVariable String sessionId,
            @Valid @RequestBody ToggleVisibilityRequest request,
            Authentication authentication) {
        
        Long userId = (Long) authentication.getDetails();
        log.info("User {} toggling visibility for session {} to public={}", 
                userId, sessionId, request.getIsPublic());
        
        SessionResponse response = chatSessionService.toggleSessionVisibility(
                sessionId, userId, request.getIsPublic());
        return ResponseEntity.ok(response);
    }

}
