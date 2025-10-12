package com.g4.chatbot.controllers;

import com.g4.chatbot.dto.admin.*;
import com.g4.chatbot.services.UserManagementService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/users")
@Slf4j
public class UserManagementController {
    
    @Autowired
    private UserManagementService userManagementService;
    
    /**
     * Get all users with pagination
     * GET /api/v1/admin/users?page=0&size=10&sortBy=createdAt&sortDirection=desc
     */
    @GetMapping
    public ResponseEntity<UserListResponse> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        
        Long adminId = (Long) authentication.getDetails();
        log.info("Admin {} fetching all users - page: {}, size: {}", adminId, page, size);
        
        UserListResponse response = userManagementService.getAllUsers(page, size, sortBy, sortDirection, adminId, httpRequest);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Search users
     * GET /api/v1/admin/users/search?q=john&page=0&size=10
     */
    @GetMapping("/search")
    public ResponseEntity<UserListResponse> searchUsers(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        
        Long adminId = (Long) authentication.getDetails();
        log.info("Admin {} searching users with term: {}", adminId, q);
        
        UserListResponse response = userManagementService.searchUsers(q, page, size);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get user by ID
     * GET /api/v1/admin/users/{userId}
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserManagementDTO> getUserById(
            @PathVariable Long userId,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        
        Long adminId = (Long) authentication.getDetails();
        log.info("Admin {} fetching user {}", adminId, userId);
        
        UserManagementDTO user = userManagementService.getUserById(userId, adminId, httpRequest);
        return ResponseEntity.ok(user);
    }
    
    /**
     * Create new user
     * POST /api/v1/admin/users
     */
    @PostMapping
    public ResponseEntity<UserManagementDTO> createUser(
            @Valid @RequestBody CreateUserRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        
        Long adminId = (Long) authentication.getDetails();
        log.info("Admin {} creating new user: {}", adminId, request.getUsername());
        
        UserManagementDTO user = userManagementService.createUser(adminId, request, httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
    
    /**
     * Update user
     * PUT /api/v1/admin/users/{userId}
     */
    @PutMapping("/{userId}")
    public ResponseEntity<UserManagementDTO> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        
        Long adminId = (Long) authentication.getDetails();
        log.info("Admin {} updating user {}", adminId, userId);
        
        UserManagementDTO user = userManagementService.updateUser(adminId, userId, request, httpRequest);
        return ResponseEntity.ok(user);
    }
    
    /**
     * Delete user (soft delete)
     * DELETE /api/v1/admin/users/{userId}
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> deleteUser(
            @PathVariable Long userId,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        
        Long adminId = (Long) authentication.getDetails();
        log.info("Admin {} deleting user {}", adminId, userId);
        
        userManagementService.deleteUser(adminId, userId, httpRequest);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "User deactivated successfully");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Activate user
     * POST /api/v1/admin/users/{userId}/activate
     */
    @PostMapping("/{userId}/activate")
    public ResponseEntity<UserManagementDTO> activateUser(
            @PathVariable Long userId,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        
        Long adminId = (Long) authentication.getDetails();
        log.info("Admin {} activating user {}", adminId, userId);
        
        UserManagementDTO user = userManagementService.activateUser(adminId, userId, httpRequest);
        return ResponseEntity.ok(user);
    }
    
    /**
     * Deactivate user
     * POST /api/v1/admin/users/{userId}/deactivate
     */
    @PostMapping("/{userId}/deactivate")
    public ResponseEntity<UserManagementDTO> deactivateUser(
            @PathVariable Long userId,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        
        Long adminId = (Long) authentication.getDetails();
        log.info("Admin {} deactivating user {}", adminId, userId);
        
        UserManagementDTO user = userManagementService.deactivateUser(adminId, userId, httpRequest);
        return ResponseEntity.ok(user);
    }
    
    /**
     * Reset user password
     * POST /api/v1/admin/users/{userId}/reset-password
     */
    @PostMapping("/{userId}/reset-password")
    public ResponseEntity<Map<String, Object>> resetUserPassword(
            @PathVariable Long userId,
            @Valid @RequestBody ResetUserPasswordRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        
        Long adminId = (Long) authentication.getDetails();
        log.info("Admin {} resetting password for user {}", adminId, userId);
        
        userManagementService.resetUserPassword(adminId, userId, request, httpRequest);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "User password reset successfully");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Unlock user account
     * POST /api/v1/admin/users/{userId}/unlock
     */
    @PostMapping("/{userId}/unlock")
    public ResponseEntity<UserManagementDTO> unlockUser(
            @PathVariable Long userId,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        
        Long adminId = (Long) authentication.getDetails();
        log.info("Admin {} unlocking user {}", adminId, userId);
        
        UserManagementDTO user = userManagementService.unlockUser(adminId, userId, httpRequest);
        return ResponseEntity.ok(user);
    }
}
