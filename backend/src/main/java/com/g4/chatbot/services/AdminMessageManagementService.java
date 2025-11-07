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
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Autowired
    private AdminActivityLogger adminActivityLogger;

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
            Long adminId,
            HttpServletRequest httpRequest) {

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

        // Log activity
        Map<String, Object> details = new HashMap<>();
        details.put("page", page);
        details.put("size", size);
        details.put("sortBy", sortBy);
        details.put("sortDirection", sortDirection);
        if (sessionId != null) details.put("sessionId", sessionId);
        if (userId != null) details.put("userId", userId);
        if (role != null) details.put("role", role);
        if (isFlagged != null) details.put("isFlagged", isFlagged);
        details.put("resultCount", messagePage.getContent().size());
        details.put("totalElements", messagePage.getTotalElements());
        
        adminActivityLogger.logActivity(
                adminId,
                "READ",
                "Message",
                "list",
                details,
                httpRequest
        );

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
    public AdminMessageDTO getMessageById(Long messageId, Long adminId, HttpServletRequest httpRequest) {
        log.info("Admin {} fetching message: {}", adminId, messageId);

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with ID: " + messageId));

        // Log activity
        Map<String, Object> details = new HashMap<>();
        details.put("messageId", messageId);
        details.put("sessionId", message.getSessionId());
        details.put("role", message.getRole().toString());
        details.put("isFlagged", message.getIsFlagged());
        
        adminActivityLogger.logActivity(
                adminId,
                "READ",
                "Message",
                messageId.toString(),
                details,
                httpRequest
        );

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
            Long adminId,
            HttpServletRequest httpRequest) {

        log.info("Admin {} fetching messages for session: {}", adminId, sessionId);

        // Verify session exists
        chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with ID: " + sessionId));

        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Message> messagePage = messageRepository.findBySessionId(sessionId, pageable);

        // Log activity
        Map<String, Object> details = new HashMap<>();
        details.put("sessionId", sessionId);
        details.put("page", page);
        details.put("size", size);
        details.put("sortBy", sortBy);
        details.put("sortDirection", sortDirection);
        details.put("resultCount", messagePage.getContent().size());
        details.put("totalElements", messagePage.getTotalElements());
        
        adminActivityLogger.logActivity(
                adminId,
                "READ",
                "Message",
                "session:" + sessionId,
                details,
                httpRequest
        );

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
    public void deleteMessage(Long messageId, Long adminId, HttpServletRequest request) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new UnauthorizedException("Admin not found"));

        if (admin.getLevel() > 1) {
            throw new UnauthorizedException("Only Level 0-1 admins can delete messages");
        }

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with ID: " + messageId));

        log.info("Admin {} (Level {}) deleting message: {}", adminId, admin.getLevel(), messageId);

        // Collect details for logging
        Map<String, Object> details = new HashMap<>();
        details.put("role", message.getRole().toString());
        details.put("content_preview", message.getContent() != null && message.getContent().length() > 50 
            ? message.getContent().substring(0, 50) + "..." 
            : message.getContent());
        details.put("sessionId", message.getSessionId());
        details.put("tokenCount", message.getTokenCount());
        
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
                        details.put("also_deleted_assistant_message", msg.getId());
                        break;
                    }
                }
            }

            session.setMessageCount(Math.max(0, session.getMessageCount() - messagesDeleted));
            session.setTokenUsage(Math.max(0, session.getTokenUsage() - tokensDeleted));
            session.setUpdatedAt(LocalDateTime.now());
            chatSessionRepository.save(session);
            
            details.put("messages_deleted_count", messagesDeleted);
            details.put("tokens_deleted", tokensDeleted);
        }

        messageRepository.delete(message);
        
        // Log the activity
        adminActivityLogger.logActivity(adminId, "DELETE", "Message", messageId.toString(), details, request);

        log.info("Message {} successfully deleted by admin {}", messageId, adminId);
    }

    /**
     * Flag a message for review
     * Level 2 admins (moderators) and above can flag
     */
    @Transactional
    public AdminMessageDTO flagMessage(Long messageId, FlagMessageRequest request, Long adminId, HttpServletRequest httpRequest) {
        log.info("Admin {} flagging message: {} - Type: {}", adminId, messageId, request.getFlagType());

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with ID: " + messageId));

        message.setIsFlagged(true);
        message.setFlagReason(request.getFlagType() + ": " + request.getReason());
        message.setFlaggedBy(adminId);
        message.setFlaggedAt(LocalDateTime.now());

        Message updated = messageRepository.save(message);
        
        // Log the activity
        Map<String, Object> details = new HashMap<>();
        details.put("flagType", request.getFlagType());
        details.put("reason", request.getReason());
        details.put("sessionId", message.getSessionId());
        details.put("role", message.getRole().toString());
        adminActivityLogger.logActivity(adminId, "FLAG", "Message", messageId.toString(), details, httpRequest);

        log.info("Message {} flagged by admin {} - Reason: {}", messageId, adminId, request.getReason());

        return convertToAdminDTO(updated);
    }

    /**
     * Unflag a message
     * Level 2 admins and above can unflag
     */
    @Transactional
    public AdminMessageDTO unflagMessage(Long messageId, Long adminId, HttpServletRequest httpRequest) {
        log.info("Admin {} unflagging message: {}", adminId, messageId);

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with ID: " + messageId));

        message.setIsFlagged(false);
        message.setFlagReason(null);
        message.setFlaggedBy(null);
        message.setFlaggedAt(null);

        Message updated = messageRepository.save(message);
        
        // Log the activity
        Map<String, Object> details = new HashMap<>();
        details.put("sessionId", message.getSessionId());
        details.put("role", message.getRole().toString());
        adminActivityLogger.logActivity(adminId, "UNFLAG", "Message", messageId.toString(), details, httpRequest);

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
