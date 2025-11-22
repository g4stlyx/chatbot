package com.g4.chatbot.controllers;

import com.g4.chatbot.dto.admin.AuthErrorLogListResponse;
import com.g4.chatbot.dto.admin.AuthErrorLogResponse;
import com.g4.chatbot.dto.admin.AuthErrorStatisticsResponse;
import com.g4.chatbot.models.Admin;
import com.g4.chatbot.repos.AdminRepository;
import com.g4.chatbot.security.JwtUtils;
import com.g4.chatbot.services.AdminAuthErrorService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller for admin management of authentication error logs
 * Only accessible by admins (level 0, 1, 2)
 */
@RestController
@RequestMapping("/api/v1/admin/auth-error-logs")
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminAuthErrorController {
    
    @Autowired
    private AdminAuthErrorService adminAuthErrorService;
    
    @Autowired
    private AdminRepository adminRepository;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    /**
     * GET /api/v1/admin/auth-error-logs
     * Get all authentication error logs with pagination and filtering
     */
    @GetMapping
    public ResponseEntity<AuthErrorLogListResponse> getAllLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String errorType,
            @RequestParam(required = false) String ipAddress,
            Authentication authentication,
            HttpServletRequest request) {
        
        Long adminId = (Long) authentication.getDetails();
        
        log.info("Admin {} requesting auth error logs", adminId);
        
        AuthErrorLogListResponse response = adminAuthErrorService.getAllLogs(
                adminId, page, size, sortBy, sortDirection, userId, errorType, ipAddress, request
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/v1/admin/auth-error-logs/{id}
     * Get a single authentication error log by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<AuthErrorLogResponse> getLogById(
            @PathVariable Long id,
            Authentication authentication,
            HttpServletRequest request) {
        
        Long adminId = (Long) authentication.getDetails();
        
        log.info("Admin {} requesting auth error log: {}", adminId, id);
        
        AuthErrorLogResponse response = adminAuthErrorService.getLogById(
                adminId, id, request
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/v1/admin/auth-error-logs/user/{userId}
     * Get authentication error logs by user ID
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<AuthErrorLogListResponse> getLogsByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication,
            HttpServletRequest request) {
        
        Long adminId = (Long) authentication.getDetails();
        
        log.info("Admin {} requesting auth error logs for user: {}", adminId, userId);
        
        AuthErrorLogListResponse response = adminAuthErrorService.getLogsByUserId(
                adminId, userId, page, size, request
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/v1/admin/auth-error-logs/ip/{ipAddress}
     * Get authentication error logs by IP address
     */
    @GetMapping("/ip/{ipAddress}")
    public ResponseEntity<AuthErrorLogListResponse> getLogsByIpAddress(
            @PathVariable String ipAddress,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication,
            HttpServletRequest request) {
        
        Long adminId = (Long) authentication.getDetails();
        
        log.info("Admin {} requesting auth error logs for IP: {}", adminId, ipAddress);
        
        AuthErrorLogListResponse response = adminAuthErrorService.getLogsByIpAddress(
                adminId, ipAddress, page, size, request
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/v1/admin/auth-error-logs/statistics
     * Get authentication error statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<AuthErrorStatisticsResponse> getStatistics(
            Authentication authentication,
            HttpServletRequest request) {
        
        Long adminId = (Long) authentication.getDetails();
        
        log.info("Admin {} requesting auth error statistics", adminId);
        
        AuthErrorStatisticsResponse response = adminAuthErrorService.getStatistics(
                adminId, request
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * DELETE /api/v1/admin/auth-error-logs/{id}
     * Delete an authentication error log (Level 0 admins only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteLog(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token,
            Authentication authentication,
            HttpServletRequest request) {
        
        try {
            Long adminId = (Long) authentication.getDetails();
            
            // Verify Level 0 admin (manual check since @PreAuthorize can't access bean methods)
            String jwtToken = token.substring(7);
            Long tokenAdminId = jwtUtils.extractUserIdAsLong(jwtToken);
            
            Admin admin = adminRepository.findById(tokenAdminId)
                    .orElseThrow(() -> new RuntimeException("Admin not found"));
            
            if (admin.getLevel() != 0) {
                return ResponseEntity.status(403).body(
                        Map.of("error", "Only Level 0 admins can delete authentication error logs")
                );
            }
            
            adminAuthErrorService.deleteLog(adminId, id, request);
            
            return ResponseEntity.ok(Map.of("message", "Authentication error log deleted successfully"));
            
        } catch (Exception e) {
            log.error("Error deleting auth error log: ", e);
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }
}
