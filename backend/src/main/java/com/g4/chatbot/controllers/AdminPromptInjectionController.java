package com.g4.chatbot.controllers;

import com.g4.chatbot.dto.admin.PromptInjectionLogListResponse;
import com.g4.chatbot.dto.admin.PromptInjectionLogResponse;
import com.g4.chatbot.models.Admin;
import com.g4.chatbot.repos.AdminRepository;
import com.g4.chatbot.security.JwtUtils;
import com.g4.chatbot.services.AdminPromptInjectionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller for admin management of prompt injection logs
 * Only accessible by admins (level 0, 1, 2)
 */
@RestController
@RequestMapping("/api/v1/admin/prompt-injection-logs")
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminPromptInjectionController {
    
    @Autowired
    private AdminPromptInjectionService adminPromptInjectionService;
    
    @Autowired
    private AdminRepository adminRepository;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    /**
     * GET /api/v1/admin/prompt-injection-logs
     * Get all prompt injection logs with pagination and filtering
     */
    @GetMapping
    public ResponseEntity<PromptInjectionLogListResponse> getAllLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String severity,
            Authentication authentication,
            HttpServletRequest request) {
        
        Long adminId = (Long) authentication.getDetails();
        
        log.info("Admin {} requesting prompt injection logs", adminId);
        
        PromptInjectionLogListResponse response = adminPromptInjectionService.getAllLogs(
                adminId, page, size, sortBy, sortDirection, userId, severity, request
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/v1/admin/prompt-injection-logs/{id}
     * Get a single prompt injection log by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<PromptInjectionLogResponse> getLogById(
            @PathVariable Long id,
            Authentication authentication,
            HttpServletRequest request) {
        
        Long adminId = (Long) authentication.getDetails();
        
        log.info("Admin {} requesting prompt injection log: {}", adminId, id);
        
        PromptInjectionLogResponse response = adminPromptInjectionService.getLogById(
                adminId, id, request
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * DELETE /api/v1/admin/prompt-injection-logs/{id}
     * Delete a prompt injection log (Level 0 admins only)
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
            
            // Verify Level 0 admin
            Long currentAdminId = Long.valueOf(jwtUtils.extractUserId(token.substring(7)));
            Admin admin = adminRepository.findById(currentAdminId)
                    .orElseThrow(() -> new RuntimeException("Admin not found"));
            
            if (admin.getLevel() != 0) {
                return ResponseEntity.status(403).body(Map.of(
                        "success", false,
                        "message", "Access denied. Only Level 0 Super Admins can delete prompt injection logs."
                ));
            }
            
            log.info("Admin {} deleting prompt injection log: {}", adminId, id);
            
            adminPromptInjectionService.deleteLog(adminId, id, request);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Prompt injection log deleted successfully",
                    "logId", id.toString()
            ));
            
        } catch (Exception e) {
            log.error("Error deleting prompt injection log", e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Failed to delete prompt injection log: " + e.getMessage()
            ));
        }
    }
    
    /**
     * GET /api/v1/admin/prompt-injection-logs/statistics
     * Get statistics about prompt injection attempts
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics(
            Authentication authentication,
            HttpServletRequest request) {
        
        Long adminId = (Long) authentication.getDetails();
        
        log.info("Admin {} requesting prompt injection statistics", adminId);
        
        Map<String, Object> stats = adminPromptInjectionService.getStatistics(
                adminId, request
        );
        
        return ResponseEntity.ok(stats);
    }
}
