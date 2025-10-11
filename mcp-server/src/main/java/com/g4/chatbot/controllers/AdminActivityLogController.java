package com.g4.chatbot.controllers;

import com.g4.chatbot.dto.admin.AdminActivityLogDTO;
import com.g4.chatbot.dto.admin.AdminActivityLogListResponse;
import com.g4.chatbot.models.Admin;
import com.g4.chatbot.repos.AdminRepository;
import com.g4.chatbot.security.JwtUtils;
import com.g4.chatbot.services.AdminActivityLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/activity-logs")
@RequiredArgsConstructor
@Slf4j
public class AdminActivityLogController {
    
    private final AdminActivityLogService activityLogService;
    private final AdminRepository adminRepository;
    private final JwtUtils jwtUtils;
    
    /**
     * Get all activity logs with filtering and pagination
     * Only Level 0 (Super Admin) can access
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllActivityLogs(
            @RequestHeader("Authorization") String token,
            @RequestParam(required = false) Long adminId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        try {
            // Extract admin ID and verify Level 0
            Long currentAdminId = Long.valueOf(jwtUtils.extractUserId(token.substring(7)));
            Admin admin = adminRepository.findById(currentAdminId)
                    .orElseThrow(() -> new RuntimeException("Admin not found"));
            
            if (admin.getLevel() != 0) {
                return ResponseEntity.status(403).body(Map.of(
                        "success", false,
                        "message", "Access denied. Only Level 0 Super Admins can view activity logs."
                ));
            }
            
            AdminActivityLogListResponse response = activityLogService.getAllActivityLogs(
                    adminId, action, resourceType, startDate, page, size, sortBy, sortDirection
            );
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", response
            ));
            
        } catch (Exception e) {
            log.error("Error fetching activity logs", e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Failed to fetch activity logs: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get activity log by ID
     * Only Level 0 (Super Admin) can access
     */
    @GetMapping("/{logId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getActivityLogById(
            @RequestHeader("Authorization") String token,
            @PathVariable Long logId
    ) {
        try {
            // Extract admin ID and verify Level 0
            Long currentAdminId = Long.valueOf(jwtUtils.extractUserId(token.substring(7)));
            Admin admin = adminRepository.findById(currentAdminId)
                    .orElseThrow(() -> new RuntimeException("Admin not found"));
            
            if (admin.getLevel() != 0) {
                return ResponseEntity.status(403).body(Map.of(
                        "success", false,
                        "message", "Access denied. Only Level 0 Super Admins can view activity logs."
                ));
            }
            
            AdminActivityLogDTO logDTO = activityLogService.getActivityLogById(logId);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", logDTO
            ));
            
        } catch (RuntimeException e) {
            log.error("Error fetching activity log by ID: {}", logId, e);
            return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error fetching activity log by ID: {}", logId, e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Failed to fetch activity log: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Delete activity log
     * Only Level 0 (Super Admin) can access
     */
    @DeleteMapping("/{logId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteActivityLog(
            @RequestHeader("Authorization") String token,
            @PathVariable Long logId
    ) {
        try {
            // Extract admin ID and verify Level 0
            Long currentAdminId = Long.valueOf(jwtUtils.extractUserId(token.substring(7)));
            Admin admin = adminRepository.findById(currentAdminId)
                    .orElseThrow(() -> new RuntimeException("Admin not found"));
            
            if (admin.getLevel() != 0) {
                return ResponseEntity.status(403).body(Map.of(
                        "success", false,
                        "message", "Access denied. Only Level 0 Super Admins can delete activity logs."
                ));
            }
            
            activityLogService.deleteActivityLog(logId);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Activity log deleted successfully"
            ));
            
        } catch (RuntimeException e) {
            log.error("Error deleting activity log: {}", logId, e);
            return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error deleting activity log: {}", logId, e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Failed to delete activity log: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get activity statistics for an admin
     * Only Level 0 (Super Admin) can access
     */
    @GetMapping("/stats/{adminId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getActivityStats(
            @RequestHeader("Authorization") String token,
            @PathVariable Long adminId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since
    ) {
        try {
            // Extract admin ID and verify Level 0
            Long currentAdminId = Long.valueOf(jwtUtils.extractUserId(token.substring(7)));
            Admin admin = adminRepository.findById(currentAdminId)
                    .orElseThrow(() -> new RuntimeException("Admin not found"));
            
            if (admin.getLevel() != 0) {
                return ResponseEntity.status(403).body(Map.of(
                        "success", false,
                        "message", "Access denied. Only Level 0 Super Admins can view activity statistics."
                ));
            }
            
            LocalDateTime sinceDate = since != null ? since : LocalDateTime.now().minusDays(30);
            long count = activityLogService.getActivityCountForAdmin(adminId, sinceDate);
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("adminId", adminId);
            stats.put("activityCount", count);
            stats.put("since", sinceDate);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", stats
            ));
            
        } catch (Exception e) {
            log.error("Error fetching activity stats for admin: {}", adminId, e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Failed to fetch activity statistics: " + e.getMessage()
            ));
        }
    }
}
