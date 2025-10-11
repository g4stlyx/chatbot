package com.g4.chatbot.services;

import com.g4.chatbot.dto.admin.message.AdminMessageDTO;
import com.g4.chatbot.dto.admin.message.AdminMessageListResponse;
import com.g4.chatbot.dto.admin.message.FlagMessageRequest;
import com.g4.chatbot.exception.ResourceNotFoundException;
import com.g4.chatbot.exception.UnauthorizedException;
import com.g4.chatbot.models.Admin;
import com.g4.chatbot.models.ChatSession;
import com.g4.chatbot.models.Message;
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
import java.util.List;

/**
 * Service for admin message management operations
 */
@Service
@Slf4j
public class AdminMessageManagementService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ChatSessionRepository chatSessionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminRepository adminRepository;

    /**
     * Get all messages with optional filtering
     * Level 2 admins (moderators) and above can access
     */
    public AdminMessageListResponse getAllMessages(
            String sessionId,
            Long userId,
            String role,
            Boolean isFlagged,
            int page,
            int size,
            String sortBy,
            String sortDirection,
            Long adminId) {

        log.info("Admin {} fetching all messages - page: {}, size: {}", adminId, page, size);

        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Message> messagePage;

        // Apply filters
        if (sessionId != null && isFlagged != null) {
            messagePage = messageRepository.findBySessionIdAndIsFlagged(sessionId, isFlagged, pageable);
        } else if (role != null && isFlagged != null) {
            messagePage = messageRepository.findByRoleAndIsFlagged(
                    Message.MessageRole.valueOf(role.toUpperCase()), isFlagged, pageable);
        } else if (sessionId != null) {
            messagePage = messageRepository.findBySessionId(sessionId, pageable);
        } else if (userId != null) {
            messagePage = messageRepository.findByUserId(userId, pageable);
        } else if (role != null) {
            messagePage = messageRepository.findByRole(
                    Message.MessageRole.valueOf(role.toUpperCase()), pageable);
        } else if (isFlagged != null) {
            messagePage = messageRepository.findByIsFlagged(isFlagged, pageable);
        } else {
            messagePage = messageRepository.findAll(pageable);
        }

        return AdminMessageListResponse.builder()
                .messages(messagePage.getContent().stream()
                        .map(this::convertToAdminDTO)
                        .toList())
                .currentPage(messagePage.getNumber())
                .totalPages(messagePage.getTotalPages())
                .totalElements(messagePage.getTotalElements())
                .pageSize(messagePage.getSize())
                .build();
    }

    /**
     * Get message by ID with full context
     */
    public AdminMessageDTO getMessageById(Long messageId, Long adminId) {
        log.info("Admin {} fetching message: {}", adminId, messageId);

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with ID: " + messageId));

        return convertToAdminDTO(message);
    }

    /**
     * Get all messages for a specific session
     */
    public AdminMessageListResponse getMessagesBySession(
            String sessionId,
            int page,
            int size,
            String sortBy,
            String sortDirection,
            Long adminId) {

        log.info("Admin {} fetching messages for session: {}", adminId, sessionId);

        // Verify session exists
        chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with ID: " + sessionId));

        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Message> messagePage = messageRepository.findBySessionId(sessionId, pageable);

        return AdminMessageListResponse.builder()
                .messages(messagePage.getContent().stream()
                        .map(this::convertToAdminDTO)
                        .toList())
                .currentPage(messagePage.getNumber())
                .totalPages(messagePage.getTotalPages())
                .totalElements(messagePage.getTotalElements())
                .pageSize(messagePage.getSize())
                .build();
    }

    /**
     * Delete a message permanently
     * Only Level 0-1 admins can delete messages
     * If deleting a USER message, also deletes the corresponding ASSISTANT response
     */
    @Transactional
    public void deleteMessage(Long messageId, Long adminId) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new UnauthorizedException("Admin not found"));

        if (admin.getLevel() > 1) {
            throw new UnauthorizedException("Only Level 0-1 admins can delete messages");
        }

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with ID: " + messageId));

        log.info("Admin {} (Level {}) deleting message: {}", adminId, admin.getLevel(), messageId);

        // Update session message count and token usage
        ChatSession session = chatSessionRepository.findById(message.getSessionId()).orElse(null);
        if (session != null) {
            int messagesDeleted = 1;
            int tokensDeleted = message.getTokenCount() != null ? message.getTokenCount() : 0;

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
                        tokensDeleted += (msg.getTokenCount() != null ? msg.getTokenCount() : 0);
                        messagesDeleted++;
                        messageRepository.delete(msg);
                        break;
                    }
                }
            }

            session.setMessageCount(Math.max(0, session.getMessageCount() - messagesDeleted));
            session.setTokenUsage(Math.max(0, session.getTokenUsage() - tokensDeleted));
            session.setUpdatedAt(LocalDateTime.now());
            chatSessionRepository.save(session);
        }

        messageRepository.delete(message);

        log.info("Message {} successfully deleted by admin {}", messageId, adminId);
    }

    /**
     * Flag a message for review
     * Level 2 admins (moderators) and above can flag
     */
    @Transactional
    public AdminMessageDTO flagMessage(Long messageId, FlagMessageRequest request, Long adminId) {
        log.info("Admin {} flagging message: {} - Type: {}", adminId, messageId, request.getFlagType());

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with ID: " + messageId));

        message.setIsFlagged(true);
        message.setFlagReason(request.getFlagType() + ": " + request.getReason());
        message.setFlaggedBy(adminId);
        message.setFlaggedAt(LocalDateTime.now());

        Message updated = messageRepository.save(message);

        log.info("Message {} flagged by admin {} - Reason: {}", messageId, adminId, request.getReason());

        return convertToAdminDTO(updated);
    }

    /**
     * Unflag a message
     * Level 2 admins and above can unflag
     */
    @Transactional
    public AdminMessageDTO unflagMessage(Long messageId, Long adminId) {
        log.info("Admin {} unflagging message: {}", adminId, messageId);

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with ID: " + messageId));

        message.setIsFlagged(false);
        message.setFlagReason(null);
        message.setFlaggedBy(null);
        message.setFlaggedAt(null);

        Message updated = messageRepository.save(message);

        log.info("Message {} unflagged by admin {}", messageId, adminId);

        return convertToAdminDTO(updated);
    }

    /**
     * Convert Message entity to AdminMessageDTO
     */
    private AdminMessageDTO convertToAdminDTO(Message message) {
        ChatSession session = chatSessionRepository.findById(message.getSessionId()).orElse(null);
        User user = session != null ? userRepository.findById(session.getUserId()).orElse(null) : null;

        return AdminMessageDTO.builder()
                .id(message.getId())
                .sessionId(message.getSessionId())
                .sessionTitle(session != null ? session.getTitle() : "Unknown")
                .userId(session != null ? session.getUserId() : null)
                .username(user != null ? user.getUsername() : "Unknown")
                .userEmail(user != null ? user.getEmail() : "Unknown")
                .role(message.getRole().name())
                .content(message.getContent())
                .tokenCount(message.getTokenCount())
                .model(message.getModel())
                .isFlagged(message.getIsFlagged())
                .flagReason(message.getFlagReason())
                .timestamp(message.getTimestamp())
                .build();
    }
}
