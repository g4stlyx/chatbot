package com.g4.chatbot.services;

import com.g4.chatbot.dto.admin.session.AdminChatSessionDTO;
import com.g4.chatbot.dto.admin.session.AdminSessionListResponse;
import com.g4.chatbot.dto.admin.session.FlagSessionRequest;
import com.g4.chatbot.exception.ResourceNotFoundException;
import com.g4.chatbot.exception.UnauthorizedException;
import com.g4.chatbot.models.Admin;
import com.g4.chatbot.models.ChatSession;
import com.g4.chatbot.models.User;
import com.g4.chatbot.repos.AdminRepository;
import com.g4.chatbot.repos.ChatSessionRepository;
import com.g4.chatbot.repos.MessageRepository;
import com.g4.chatbot.repos.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service for admin chat session management operations
 */
@Service
@Slf4j
public class AdminSessionManagementService {

    @Autowired
    private ChatSessionRepository chatSessionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private MessageRepository messageRepository;

    /**
     * Get all chat sessions with optional filtering
     * Level 2 admins (moderators) and above can access
     */
    public AdminSessionListResponse getAllSessions(
            Long userId,
            String status,
            Boolean isFlagged,
            Boolean isPublic,
            int page,
            int size,
            String sortBy,
            String sortDirection,
            Long adminId) {

        log.info("Admin {} fetching all sessions - page: {}, size: {}", adminId, page, size);

        Sort sort = sortDirection.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ChatSession> sessionPage;

        // Apply filters
        if (userId != null && status != null && isFlagged != null) {
            sessionPage = chatSessionRepository.findByUserIdAndStatusAndIsFlagged(
                userId, ChatSession.SessionStatus.valueOf(status.toUpperCase()), isFlagged, pageable);
        } else if (userId != null && status != null) {
            sessionPage = chatSessionRepository.findByUserIdAndStatus(
                userId, ChatSession.SessionStatus.valueOf(status.toUpperCase()), pageable);
        } else if (userId != null && isFlagged != null) {
            sessionPage = chatSessionRepository.findByUserIdAndIsFlagged(userId, isFlagged, pageable);
        } else if (status != null && isFlagged != null) {
            sessionPage = chatSessionRepository.findByStatusAndIsFlagged(
                ChatSession.SessionStatus.valueOf(status.toUpperCase()), isFlagged, pageable);
        } else if (userId != null) {
            sessionPage = chatSessionRepository.findByUserId(userId, pageable);
        } else if (status != null) {
            sessionPage = chatSessionRepository.findByStatus(
                ChatSession.SessionStatus.valueOf(status.toUpperCase()), pageable);
        } else if (isFlagged != null) {
            sessionPage = chatSessionRepository.findByIsFlagged(isFlagged, pageable);
        } else if (isPublic != null) {
            sessionPage = chatSessionRepository.findByIsPublic(isPublic, pageable);
        } else {
            sessionPage = chatSessionRepository.findAll(pageable);
        }

        return AdminSessionListResponse.builder()
                .sessions(sessionPage.getContent().stream()
                        .map(this::convertToAdminDTO)
                        .toList())
                .currentPage(sessionPage.getNumber())
                .totalPages(sessionPage.getTotalPages())
                .totalElements(sessionPage.getTotalElements())
                .pageSize(sessionPage.getSize())
                .build();
    }

    /**
     * Get session by ID with full details
     */
    public AdminChatSessionDTO getSessionById(String sessionId, Long adminId) {
        log.info("Admin {} fetching session: {}", adminId, sessionId);

        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with ID: " + sessionId));

        return convertToAdminDTO(session);
    }

    /**
     * Delete a session permanently (hard delete)
     * Only Level 0-1 admins can perform this action
     */
    @Transactional
    public void deleteSession(String sessionId, Long adminId) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new UnauthorizedException("Admin not found"));

        if (admin.getLevel() > 1) {
            throw new UnauthorizedException("Only Level 0-1 admins can delete sessions");
        }

        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with ID: " + sessionId));

        log.info("Admin {} (Level {}) deleting session: {}", adminId, admin.getLevel(), sessionId);

        // Delete all messages first (cascade)
        messageRepository.deleteBySessionId(sessionId);

        // Delete session
        chatSessionRepository.delete(session);

        log.info("Session {} successfully deleted by admin {}", sessionId, adminId);
    }

    /**
     * Archive a session (soft delete)
     * Level 2 admins (moderators) and above can perform this
     */
    @Transactional
    public AdminChatSessionDTO archiveSession(String sessionId, Long adminId) {
        log.info("Admin {} archiving session: {}", adminId, sessionId);

        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with ID: " + sessionId));

        session.setStatus(ChatSession.SessionStatus.ARCHIVED);
        session.setUpdatedAt(LocalDateTime.now());

        ChatSession updated = chatSessionRepository.save(session);

        log.info("Session {} archived by admin {}", sessionId, adminId);

        return convertToAdminDTO(updated);
    }

    /**
     * Flag a session for review
     * Level 2 admins (moderators) and above can flag
     */
    @Transactional
    public AdminChatSessionDTO flagSession(String sessionId, FlagSessionRequest request, Long adminId) {
        log.info("Admin {} flagging session: {} - Type: {}", adminId, sessionId, request.getFlagType());

        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with ID: " + sessionId));

        session.setIsFlagged(true);
        session.setFlagReason(request.getFlagType() + ": " + request.getReason());
        session.setFlaggedBy(adminId);
        session.setFlaggedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());

        ChatSession updated = chatSessionRepository.save(session);

        log.info("Session {} flagged by admin {} - Reason: {}", sessionId, adminId, request.getReason());

        return convertToAdminDTO(updated);
    }

    /**
     * Unflag a session
     * Level 2 admins and above can unflag
     */
    @Transactional
    public AdminChatSessionDTO unflagSession(String sessionId, Long adminId) {
        log.info("Admin {} unflagging session: {}", adminId, sessionId);

        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with ID: " + sessionId));

        session.setIsFlagged(false);
        session.setFlagReason(null);
        session.setFlaggedBy(null);
        session.setFlaggedAt(null);
        session.setUpdatedAt(LocalDateTime.now());

        ChatSession updated = chatSessionRepository.save(session);

        log.info("Session {} unflagged by admin {}", sessionId, adminId);

        return convertToAdminDTO(updated);
    }

    /**
     * Convert ChatSession entity to AdminChatSessionDTO
     */
    private AdminChatSessionDTO convertToAdminDTO(ChatSession session) {
        User user = userRepository.findById(session.getUserId()).orElse(null);

        return AdminChatSessionDTO.builder()
                .sessionId(session.getSessionId())
                .userId(session.getUserId())
                .username(user != null ? user.getUsername() : "Unknown")
                .userEmail(user != null ? user.getEmail() : "Unknown")
                .title(session.getTitle())
                .model(session.getModel())
                .status(session.getStatus().name())
                .messageCount(session.getMessageCount())
                .tokenUsage(session.getTokenUsage())
                .isPublic(session.getIsPublic())
                .isFlagged(session.getIsFlagged())
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                .lastAccessedAt(session.getLastAccessedAt())
                .expiresAt(session.getExpiresAt())
                .build();
    }
}
