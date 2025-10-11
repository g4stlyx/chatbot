package com.g4.chatbot.controllers;

import com.g4.chatbot.dto.admin.message.AdminMessageDTO;
import com.g4.chatbot.dto.admin.message.AdminMessageListResponse;
import com.g4.chatbot.dto.admin.message.FlagMessageRequest;
import com.g4.chatbot.security.JwtUtils;
import com.g4.chatbot.services.AdminMessageManagementService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for admin message management operations
 * Only admins (Level 2 and above) can access these endpoints
 */
@RestController
@RequestMapping("/api/v1/admin/messages")
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class AdminMessageController {

    @Autowired
    private AdminMessageManagementService adminMessageManagementService;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * Get all messages with optional filtering
     * GET /api/v1/admin/messages?sessionId=xxx&userId=1&role=USER&isFlagged=true&page=0&size=10&sortBy=timestamp&sortDirection=desc
     */
    @GetMapping
    public ResponseEntity<AdminMessageListResponse> getAllMessages(
            @RequestParam(required = false) String sessionId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean isFlagged,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestHeader("Authorization") String token) {

        Long adminId = Long.valueOf(jwtUtils.extractUserId(token.substring(7)));

        log.info("Admin {} requesting all messages - page: {}, size: {}, filters: sessionId={}, userId={}, role={}, isFlagged={}",
                adminId, page, size, sessionId, userId, role, isFlagged);

        AdminMessageListResponse response = adminMessageManagementService.getAllMessages(
                sessionId, userId, role, isFlagged, page, size, sortBy, sortDirection, adminId);

        return ResponseEntity.ok(response);
    }

    /**
     * Get message by ID with full context
     * GET /api/v1/admin/messages/{messageId}
     */
    @GetMapping("/{messageId}")
    public ResponseEntity<AdminMessageDTO> getMessageById(
            @PathVariable Long messageId,
            @RequestHeader("Authorization") String token) {

        Long adminId = Long.valueOf(jwtUtils.extractUserId(token.substring(7)));

        log.info("Admin {} requesting message: {}", adminId, messageId);

        AdminMessageDTO response = adminMessageManagementService.getMessageById(messageId, adminId);

        return ResponseEntity.ok(response);
    }

    /**
     * Get all messages for a specific session
     * GET /api/v1/admin/sessions/{sessionId}/messages
     */
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<AdminMessageListResponse> getMessagesBySession(
            @PathVariable String sessionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestHeader("Authorization") String token) {

        Long adminId = Long.valueOf(jwtUtils.extractUserId(token.substring(7)));

        log.info("Admin {} requesting messages for session: {}", adminId, sessionId);

        AdminMessageListResponse response = adminMessageManagementService.getMessagesBySession(
                sessionId, page, size, sortBy, sortDirection, adminId);

        return ResponseEntity.ok(response);
    }

    /**
     * Delete a message permanently
     * DELETE /api/v1/admin/messages/{messageId}
     * Only Level 0-1 admins can perform this action
     */
    @DeleteMapping("/{messageId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteMessage(
            @PathVariable Long messageId,
            @RequestHeader("Authorization") String token) {

        Long adminId = Long.valueOf(jwtUtils.extractUserId(token.substring(7)));

        log.info("Admin {} attempting to delete message: {}", adminId, messageId);

        adminMessageManagementService.deleteMessage(messageId, adminId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Message deleted successfully");
        response.put("messageId", messageId);

        return ResponseEntity.ok(response);
    }

    /**
     * Flag a message for review
     * POST /api/v1/admin/messages/{messageId}/flag
     */
    @PostMapping("/{messageId}/flag")
    public ResponseEntity<AdminMessageDTO> flagMessage(
            @PathVariable Long messageId,
            @Valid @RequestBody FlagMessageRequest request,
            @RequestHeader("Authorization") String token) {

        Long adminId = Long.valueOf(jwtUtils.extractUserId(token.substring(7)));

        log.info("Admin {} flagging message: {} - Type: {}", adminId, messageId, request.getFlagType());

        AdminMessageDTO response = adminMessageManagementService.flagMessage(messageId, request, adminId);

        return ResponseEntity.ok(response);
    }

    /**
     * Unflag a message
     * POST /api/v1/admin/messages/{messageId}/unflag
     */
    @PostMapping("/{messageId}/unflag")
    public ResponseEntity<AdminMessageDTO> unflagMessage(
            @PathVariable Long messageId,
            @RequestHeader("Authorization") String token) {

        Long adminId = Long.valueOf(jwtUtils.extractUserId(token.substring(7)));

        log.info("Admin {} unflagging message: {}", adminId, messageId);

        AdminMessageDTO response = adminMessageManagementService.unflagMessage(messageId, adminId);

        return ResponseEntity.ok(response);
    }
}
