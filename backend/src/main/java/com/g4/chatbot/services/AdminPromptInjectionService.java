package com.g4.chatbot.services;

import com.g4.chatbot.dto.admin.PromptInjectionLogListResponse;
import com.g4.chatbot.dto.admin.PromptInjectionLogResponse;
import com.g4.chatbot.exception.ResourceNotFoundException;
import com.g4.chatbot.models.PromptInjectionLog;
import com.g4.chatbot.repos.PromptInjectionLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for admin management of prompt injection logs
 */
@Service
@Slf4j
public class AdminPromptInjectionService {
    
    @Autowired
    private PromptInjectionLogRepository promptInjectionLogRepository;
    
    @Autowired
    private AdminActivityLogger adminActivityLogger;
    
    /**
     * Get all prompt injection logs with pagination
     */
    @Transactional
    public PromptInjectionLogListResponse getAllLogs(
            Long adminId,
            int page, 
            int size, 
            String sortBy, 
            String sortDirection,
            Long userId,
            String severity,
            HttpServletRequest request) {
        
        log.info("Admin {} retrieving prompt injection logs - page: {}, size: {}, sortBy: {}, sortDirection: {}", 
                adminId, page, size, sortBy, sortDirection);
        
        // Create sort
        Sort sort = sortDirection.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // Get logs with filters
        Page<PromptInjectionLog> logsPage;
        
        if (userId != null && severity != null) {
            PromptInjectionLog.Severity sev = PromptInjectionLog.Severity.valueOf(severity.toUpperCase());
            logsPage = promptInjectionLogRepository.findByUserIdAndSeverity(userId, sev, pageable);
        } else if (userId != null) {
            logsPage = promptInjectionLogRepository.findByUserId(userId, pageable);
        } else if (severity != null) {
            PromptInjectionLog.Severity sev = PromptInjectionLog.Severity.valueOf(severity.toUpperCase());
            logsPage = promptInjectionLogRepository.findBySeverity(sev, pageable);
        } else {
            logsPage = promptInjectionLogRepository.findAll(pageable);
        }
        
        List<PromptInjectionLogResponse> logs = logsPage.getContent().stream()
                .map(PromptInjectionLogResponse::from)
                .collect(Collectors.toList());
        
        // Log admin activity (READ operation)
        adminActivityLogger.logRead(
                adminId,
                "PROMPT_INJECTION_LOG",
                "list",
                request
        );
        
        return PromptInjectionLogListResponse.builder()
                .logs(logs)
                .currentPage(logsPage.getNumber())
                .totalPages(logsPage.getTotalPages())
                .totalElements(logsPage.getTotalElements())
                .pageSize(logsPage.getSize())
                .build();
    }
    
    /**
     * Get a single prompt injection log by ID
     */
    @Transactional
    public PromptInjectionLogResponse getLogById(Long adminId, Long logId, HttpServletRequest request) {
        log.info("Admin {} retrieving prompt injection log: {}", adminId, logId);
        
        PromptInjectionLog log = promptInjectionLogRepository.findById(logId)
                .orElseThrow(() -> new ResourceNotFoundException("Prompt injection log not found with ID: " + logId));
        
        // Log admin activity (READ operation)
        adminActivityLogger.logRead(
                adminId,
                "PROMPT_INJECTION_LOG",
                logId.toString(),
                request
        );
        
        return PromptInjectionLogResponse.from(log);
    }
    
    /**
     * Delete a prompt injection log
     */
    @Transactional
    public void deleteLog(Long adminId, Long logId, HttpServletRequest request) {
        log.info("Admin {} deleting prompt injection log: {}", adminId, logId);
        
        PromptInjectionLog injectionLog = promptInjectionLogRepository.findById(logId)
                .orElseThrow(() -> new ResourceNotFoundException("Prompt injection log not found with ID: " + logId));
        
        promptInjectionLogRepository.delete(injectionLog);
        
        // Log admin activity
        adminActivityLogger.logDelete(
                adminId,
                "PROMPT_INJECTION_LOG",
                logId.toString(),
                request
        );
        
        log.info("Prompt injection log {} deleted successfully by admin {}", logId, adminId);
    }
    
    /**
     * Get statistics about prompt injection attempts
     */
    @Transactional
    public java.util.Map<String, Object> getStatistics(Long adminId, HttpServletRequest request) {
        log.info("Admin {} retrieving prompt injection statistics", adminId);
        
        long totalAttempts = promptInjectionLogRepository.count();
        long lowSeverity = promptInjectionLogRepository.countBySeverity(PromptInjectionLog.Severity.LOW);
        long mediumSeverity = promptInjectionLogRepository.countBySeverity(PromptInjectionLog.Severity.MEDIUM);
        long highSeverity = promptInjectionLogRepository.countBySeverity(PromptInjectionLog.Severity.HIGH);
        long criticalSeverity = promptInjectionLogRepository.countBySeverity(PromptInjectionLog.Severity.CRITICAL);
        
        // Get recent logs (last 24 hours)
        java.time.LocalDateTime yesterday = java.time.LocalDateTime.now().minusHours(24);
        List<PromptInjectionLog> recentLogs = promptInjectionLogRepository.findRecentLogs(yesterday);
        
        // Log admin activity
        adminActivityLogger.logRead(
                adminId,
                "PROMPT_INJECTION_LOG",
                "statistics",
                request
        );
        
        return java.util.Map.of(
                "totalAttempts", totalAttempts,
                "severityBreakdown", java.util.Map.of(
                        "low", lowSeverity,
                        "medium", mediumSeverity,
                        "high", highSeverity,
                        "critical", criticalSeverity
                ),
                "last24Hours", recentLogs.size()
        );
    }
}
