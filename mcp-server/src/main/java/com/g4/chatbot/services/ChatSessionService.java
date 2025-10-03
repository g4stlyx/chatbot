package com.g4.chatbot.services;

import com.g4.chatbot.dto.session.*;
import com.g4.chatbot.exception.ResourceNotFoundException;
import com.g4.chatbot.exception.UnauthorizedException;
import com.g4.chatbot.models.ChatSession;
import com.g4.chatbot.repos.ChatSessionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ChatSessionService {
    
    @Autowired
    private ChatSessionRepository chatSessionRepository;
    
    /**
     * Create a new chat session for a user
     */
    @Transactional
    public SessionResponse createSession(Long userId, CreateSessionRequest request) {
        log.info("Creating new chat session for user: {}", userId);
        
        ChatSession session = new ChatSession();
        session.setSessionId(UUID.randomUUID().toString());
        session.setUserId(userId);
        session.setTitle(request.getTitle() != null ? request.getTitle() : "New Chat");
        session.setModel(request.getModel() != null ? request.getModel() : "gpt-3.5-turbo");
        session.setIsPublic(request.getIsPublic() != null ? request.getIsPublic() : false);
        session.setStatus(ChatSession.SessionStatus.ACTIVE);
        session.setMessageCount(0);
        session.setTokenUsage(0L);
        
        ChatSession savedSession = chatSessionRepository.save(session);
        log.info("Chat session created successfully: {}", savedSession.getSessionId());
        
        return SessionResponse.fromEntity(savedSession);
    }
    
    /**
     * Get a specific session by ID (only if it belongs to the user)
     */
    public SessionResponse getSession(String sessionId, Long userId) {
        log.info("Fetching session {} for user {}", sessionId, userId);
        
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + sessionId));
        
        // Verify the session belongs to the user
        if (!session.getUserId().equals(userId)) {
            log.warn("User {} attempted to access session {} owned by user {}", 
                    userId, sessionId, session.getUserId());
            throw new UnauthorizedException("You don't have permission to access this session");
        }
        
        // Update last accessed time
        session.setLastAccessedAt(LocalDateTime.now());
        chatSessionRepository.save(session);
        
        return SessionResponse.fromEntity(session);
    }
    
    /**
     * Get all sessions for a user (with pagination)
     */
    public SessionListResponse getUserSessions(Long userId, int page, int size) {
        log.info("Fetching sessions for user {} (page: {}, size: {})", userId, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ChatSession> sessionPage = chatSessionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        
        List<SessionResponse> sessions = sessionPage.getContent().stream()
                .map(SessionResponse::fromEntity)
                .collect(Collectors.toList());
        
        return new SessionListResponse(
                sessions,
                sessionPage.getTotalPages(),
                sessionPage.getTotalElements(),
                page,
                size
        );
    }
    
    /**
     * Get all active sessions for a user
     */
    public List<SessionResponse> getUserActiveSessions(Long userId) {
        log.info("Fetching active sessions for user {}", userId);
        
        List<ChatSession> activeSessions = chatSessionRepository
                .findByUserIdAndStatusOrderByCreatedAtDesc(userId, ChatSession.SessionStatus.ACTIVE);
        
        return activeSessions.stream()
                .map(SessionResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Update a session (only if it belongs to the user)
     */
    @Transactional
    public SessionResponse updateSession(String sessionId, Long userId, UpdateSessionRequest request) {
        log.info("Updating session {} for user {}", sessionId, userId);
        
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + sessionId));
        
        // Verify the session belongs to the user
        if (!session.getUserId().equals(userId)) {
            log.warn("User {} attempted to update session {} owned by user {}", 
                    userId, sessionId, session.getUserId());
            throw new UnauthorizedException("You don't have permission to update this session");
        }
        
        // Update only provided fields
        if (request.getTitle() != null) {
            session.setTitle(request.getTitle());
        }
        if (request.getModel() != null) {
            session.setModel(request.getModel());
        }
        if (request.getIsPublic() != null) {
            session.setIsPublic(request.getIsPublic());
        }
        
        ChatSession updatedSession = chatSessionRepository.save(session);
        log.info("Session {} updated successfully", sessionId);
        
        return SessionResponse.fromEntity(updatedSession);
    }
    
    /**
     * Delete a session (soft delete by changing status to DELETED)
     */
    @Transactional
    public void deleteSession(String sessionId, Long userId) {
        log.info("Deleting session {} for user {}", sessionId, userId);
        
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + sessionId));
        
        // Verify the session belongs to the user
        if (!session.getUserId().equals(userId)) {
            log.warn("User {} attempted to delete session {} owned by user {}", 
                    userId, sessionId, session.getUserId());
            throw new UnauthorizedException("You don't have permission to delete this session");
        }
        
        // Soft delete by changing status
        session.setStatus(ChatSession.SessionStatus.DELETED);
        chatSessionRepository.save(session);
        
        log.info("Session {} marked as deleted", sessionId);
    }
    
    /**
     * Archive a session
     */
    @Transactional
    public SessionResponse archiveSession(String sessionId, Long userId) {
        log.info("Archiving session {} for user {}", sessionId, userId);
        
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + sessionId));
        
        // Verify the session belongs to the user
        if (!session.getUserId().equals(userId)) {
            log.warn("User {} attempted to archive session {} owned by user {}", 
                    userId, sessionId, session.getUserId());
            throw new UnauthorizedException("You don't have permission to archive this session");
        }
        
        session.setStatus(ChatSession.SessionStatus.ARCHIVED);
        ChatSession archivedSession = chatSessionRepository.save(session);
        
        log.info("Session {} archived successfully", sessionId);
        return SessionResponse.fromEntity(archivedSession);
    }
    
    /**
     * Pause a session
     */
    @Transactional
    public SessionResponse pauseSession(String sessionId, Long userId) {
        log.info("Pausing session {} for user {}", sessionId, userId);
        
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + sessionId));
        
        // Verify the session belongs to the user
        if (!session.getUserId().equals(userId)) {
            log.warn("User {} attempted to pause session {} owned by user {}", 
                    userId, sessionId, session.getUserId());
            throw new UnauthorizedException("You don't have permission to pause this session");
        }
        
        session.setStatus(ChatSession.SessionStatus.PAUSED);
        ChatSession pausedSession = chatSessionRepository.save(session);
        
        log.info("Session {} paused successfully", sessionId);
        return SessionResponse.fromEntity(pausedSession);
    }
    
    /**
     * Activate/Resume a session
     */
    @Transactional
    public SessionResponse activateSession(String sessionId, Long userId) {
        log.info("Activating session {} for user {}", sessionId, userId);
        
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + sessionId));
        
        // Verify the session belongs to the user
        if (!session.getUserId().equals(userId)) {
            log.warn("User {} attempted to activate session {} owned by user {}", 
                    userId, sessionId, session.getUserId());
            throw new UnauthorizedException("You don't have permission to activate this session");
        }
        
        session.setStatus(ChatSession.SessionStatus.ACTIVE);
        ChatSession activatedSession = chatSessionRepository.save(session);
        
        log.info("Session {} activated successfully", sessionId);
        return SessionResponse.fromEntity(activatedSession);
    }
}
