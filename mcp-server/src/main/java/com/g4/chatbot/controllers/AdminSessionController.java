package com.g4.chatbot.controllers;

import com.g4.chatbot.dto.admin.session.AdminChatSessionDTO;
import com.g4.chatbot.dto.admin.session.AdminSessionListResponse;
import com.g4.chatbot.dto.admin.session.FlagSessionRequest;
import com.g4.chatbot.security.JwtUtils;
import com.g4.chatbot.services.AdminSessionManagementService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for admin chat session management operations
 * Only admins (Level 2 and above) can access these endpoints
 */
@RestController
@RequestMapping("/api/v1/admin/sessions")
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class AdminSessionController {

    @Autowired
    private AdminSessionManagementService adminSessionManagementService;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * Get all chat sessions with optional filtering
     * GET /api/v1/admin/sessions?userId=1&status=ACTIVE&isFlagged=true&isPublic=false&page=0&size=10&sortBy=createdAt&sortDirection=desc
     */
    @GetMapping
    public ResponseEntity<AdminSessionListResponse> getAllSessions(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean isFlagged,
            @RequestParam(required = false) Boolean isPublic,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestHeader("Authorization") String token,
            HttpServletRequest httpRequest) {

        Long adminId = Long.valueOf(jwtUtils.extractUserId(token.substring(7)));

        log.info("Admin {} requesting all sessions - page: {}, size: {}, filters: userId={}, status={}, isFlagged={}, isPublic={}",
                adminId, page, size, userId, status, isFlagged, isPublic);

        AdminSessionListResponse response = adminSessionManagementService.getAllSessions(
                userId, status, isFlagged, isPublic, page, size, sortBy, sortDirection, adminId, httpRequest);

        return ResponseEntity.ok(response);
    }

    /**
     * Get session by ID with full details
     * GET /api/v1/admin/sessions/{sessionId}
     */
    @GetMapping("/{sessionId}")
    public ResponseEntity<AdminChatSessionDTO> getSessionById(
            @PathVariable String sessionId,
            @RequestHeader("Authorization") String token,
            HttpServletRequest httpRequest) {

        Long adminId = Long.valueOf(jwtUtils.extractUserId(token.substring(7)));

        log.info("Admin {} requesting session: {}", adminId, sessionId);

        AdminChatSessionDTO response = adminSessionManagementService.getSessionById(sessionId, adminId, httpRequest);

        return ResponseEntity.ok(response);
    }

    /**
     * Delete a session permanently (hard delete)
     * DELETE /api/v1/admin/sessions/{sessionId}
     * Only Level 0-1 admins can perform this action
     */
    @DeleteMapping("/{sessionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteSession(
            @PathVariable String sessionId,
            @RequestHeader("Authorization") String token,
            HttpServletRequest httpRequest) {

        Long adminId = Long.valueOf(jwtUtils.extractUserId(token.substring(7)));

        log.info("Admin {} attempting to delete session: {}", adminId, sessionId);

        adminSessionManagementService.deleteSession(sessionId, adminId, httpRequest);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Session deleted successfully");
        response.put("sessionId", sessionId);

        return ResponseEntity.ok(response);
    }

    /**
     * Archive a session (soft delete)
     * POST /api/v1/admin/sessions/{sessionId}/archive
     */
    @PostMapping("/{sessionId}/archive")
    public ResponseEntity<AdminChatSessionDTO> archiveSession(
            @PathVariable String sessionId,
            @RequestHeader("Authorization") String token,
            HttpServletRequest httpRequest) {

        Long adminId = Long.valueOf(jwtUtils.extractUserId(token.substring(7)));

        log.info("Admin {} archiving session: {}", adminId, sessionId);

        AdminChatSessionDTO response = adminSessionManagementService.archiveSession(sessionId, adminId, httpRequest);

        return ResponseEntity.ok(response);
    }

    /**
     * Flag a session for review
     * POST /api/v1/admin/sessions/{sessionId}/flag
     */
    @PostMapping("/{sessionId}/flag")
    public ResponseEntity<AdminChatSessionDTO> flagSession(
            @PathVariable String sessionId,
            @Valid @RequestBody FlagSessionRequest request,
            @RequestHeader("Authorization") String token,
            HttpServletRequest httpRequest) {

        Long adminId = Long.valueOf(jwtUtils.extractUserId(token.substring(7)));

        log.info("Admin {} flagging session: {} - Type: {}", adminId, sessionId, request.getFlagType());

        AdminChatSessionDTO response = adminSessionManagementService.flagSession(sessionId, request, adminId, httpRequest);

        return ResponseEntity.ok(response);
    }

    /**
     * Unflag a session
     * POST /api/v1/admin/sessions/{sessionId}/unflag
     */
    @PostMapping("/{sessionId}/unflag")
    public ResponseEntity<AdminChatSessionDTO> unflagSession(
            @PathVariable String sessionId,
            @RequestHeader("Authorization") String token,
            HttpServletRequest httpRequest) {

        Long adminId = Long.valueOf(jwtUtils.extractUserId(token.substring(7)));

        log.info("Admin {} unflagging session: {}", adminId, sessionId);

        AdminChatSessionDTO response = adminSessionManagementService.unflagSession(sessionId, adminId, httpRequest);

        return ResponseEntity.ok(response);
    }
}
