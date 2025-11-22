package com.g4.chatbot.services;

import com.g4.chatbot.dto.admin.AuthErrorLogListResponse;
import com.g4.chatbot.dto.admin.AuthErrorLogResponse;
import com.g4.chatbot.dto.admin.AuthErrorStatisticsResponse;
import com.g4.chatbot.exception.ResourceNotFoundException;
import com.g4.chatbot.models.AuthenticationErrorLog;
import com.g4.chatbot.repos.AuthenticationErrorLogRepository;
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
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for admin management of authentication error logs
 */
@Service
@Slf4j
public class AdminAuthErrorService {
    
    @Autowired
    private AuthenticationErrorLogRepository authErrorLogRepository;
    
    @Autowired
    private AdminActivityLogger adminActivityLogger;
    
    /**
     * Get all authentication error logs with pagination and filters
     */
    @Transactional
    public AuthErrorLogListResponse getAllLogs(
            Long adminId,
            int page, 
            int size, 
            String sortBy, 
            String sortDirection,
            Long userId,
            String errorType,
            String ipAddress,
            HttpServletRequest request) {
        
        log.info("Admin {} retrieving auth error logs - page: {}, size: {}, sortBy: {}, filters: userId={}, errorType={}, ipAddress={}", 
                adminId, page, size, sortBy, userId, errorType, ipAddress);
        
        // Create sort
        Sort sort = sortDirection.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // Get logs with filters
        Page<AuthenticationErrorLog> logsPage;
        
        if (userId != null) {
            logsPage = authErrorLogRepository.findByUserId(userId, pageable);
        } else if (errorType != null) {
            AuthenticationErrorLog.ErrorType type = AuthenticationErrorLog.ErrorType.valueOf(errorType.toUpperCase());
            logsPage = authErrorLogRepository.findByErrorType(type, pageable);
        } else if (ipAddress != null) {
            logsPage = authErrorLogRepository.findByIpAddress(ipAddress, pageable);
        } else {
            logsPage = authErrorLogRepository.findAll(pageable);
        }
        
        List<AuthErrorLogResponse> logs = logsPage.getContent().stream()
                .map(AuthErrorLogResponse::from)
                .collect(Collectors.toList());
        
        // Log admin activity (READ operation)
        adminActivityLogger.logRead(
                adminId,
                "AUTH_ERROR_LOG",
                "list",
                request
        );
        
        return AuthErrorLogListResponse.builder()
                .logs(logs)
                .currentPage(logsPage.getNumber())
                .totalPages(logsPage.getTotalPages())
                .totalElements(logsPage.getTotalElements())
                .pageSize(logsPage.getSize())
                .build();
    }
    
    /**
     * Get a single authentication error log by ID
     */
    @Transactional
    public AuthErrorLogResponse getLogById(Long adminId, Long logId, HttpServletRequest request) {
        log.info("Admin {} retrieving auth error log: {}", adminId, logId);
        
        AuthenticationErrorLog errorLog = authErrorLogRepository.findById(logId)
                .orElseThrow(() -> new ResourceNotFoundException("Authentication error log not found with ID: " + logId));
        
        // Log admin activity (READ operation)
        adminActivityLogger.logRead(
                adminId,
                "AUTH_ERROR_LOG",
                logId.toString(),
                request
        );
        
        return AuthErrorLogResponse.from(errorLog);
    }
    
    /**
     * Get logs by user ID
     */
    @Transactional
    public AuthErrorLogListResponse getLogsByUserId(
            Long adminId,
            Long userId,
            int page,
            int size,
            HttpServletRequest request) {
        
        log.info("Admin {} retrieving auth error logs for user: {}", adminId, userId);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<AuthenticationErrorLog> logsPage = authErrorLogRepository.findByUserId(userId, pageable);
        
        List<AuthErrorLogResponse> logs = logsPage.getContent().stream()
                .map(AuthErrorLogResponse::from)
                .collect(Collectors.toList());
        
        // Log admin activity
        adminActivityLogger.logRead(
                adminId,
                "AUTH_ERROR_LOG",
                "user-" + userId,
                request
        );
        
        return AuthErrorLogListResponse.builder()
                .logs(logs)
                .currentPage(logsPage.getNumber())
                .totalPages(logsPage.getTotalPages())
                .totalElements(logsPage.getTotalElements())
                .pageSize(logsPage.getSize())
                .build();
    }
    
    /**
     * Get logs by IP address
     */
    @Transactional
    public AuthErrorLogListResponse getLogsByIpAddress(
            Long adminId,
            String ipAddress,
            int page,
            int size,
            HttpServletRequest request) {
        
        log.info("Admin {} retrieving auth error logs for IP: {}", adminId, ipAddress);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<AuthenticationErrorLog> logsPage = authErrorLogRepository.findByIpAddress(ipAddress, pageable);
        
        List<AuthErrorLogResponse> logs = logsPage.getContent().stream()
                .map(AuthErrorLogResponse::from)
                .collect(Collectors.toList());
        
        // Log admin activity
        adminActivityLogger.logRead(
                adminId,
                "AUTH_ERROR_LOG",
                "ip-" + ipAddress,
                request
        );
        
        return AuthErrorLogListResponse.builder()
                .logs(logs)
                .currentPage(logsPage.getNumber())
                .totalPages(logsPage.getTotalPages())
                .totalElements(logsPage.getTotalElements())
                .pageSize(logsPage.getSize())
                .build();
    }
    
    /**
     * Get authentication error statistics
     */
    @Transactional
    public AuthErrorStatisticsResponse getStatistics(Long adminId, HttpServletRequest request) {
        log.info("Admin {} retrieving auth error statistics", adminId);
        
        // Total count
        long totalErrors = authErrorLogRepository.count();
        
        // Count by error type
        List<Object[]> typeStats = authErrorLogRepository.getStatisticsByErrorType();
        Map<String, Long> errorTypeCounts = new HashMap<>();
        for (Object[] stat : typeStats) {
            AuthenticationErrorLog.ErrorType type = (AuthenticationErrorLog.ErrorType) stat[0];
            Long count = (Long) stat[1];
            errorTypeCounts.put(type.name(), count);
        }
        
        // Daily statistics for last 30 days
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Object[]> dailyStats = authErrorLogRepository.getDailyStatistics(thirtyDaysAgo);
        Map<String, Long> dailyCounts = new HashMap<>();
        for (Object[] stat : dailyStats) {
            String date = stat[0].toString();
            Long count = (Long) stat[1];
            dailyCounts.put(date, count);
        }
        
        // Recent high-activity IPs (last 24 hours, more than 5 errors)
        LocalDateTime oneDayAgo = LocalDateTime.now().minusHours(24);
        Page<AuthenticationErrorLog> recentLogs = authErrorLogRepository.findByDateRange(
                oneDayAgo, 
                LocalDateTime.now(), 
                PageRequest.of(0, 1000)
        );
        
        // Group by IP and count
        Map<String, Long> ipCounts = recentLogs.getContent().stream()
                .filter(log -> log.getIpAddress() != null)
                .collect(Collectors.groupingBy(
                        AuthenticationErrorLog::getIpAddress,
                        Collectors.counting()
                ));
        
        // Get top 10 IPs
        Map<String, Long> topSuspiciousIps = ipCounts.entrySet().stream()
                .filter(entry -> entry.getValue() > 5)
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
        
        // Log admin activity
        adminActivityLogger.logRead(
                adminId,
                "AUTH_ERROR_LOG",
                "statistics",
                request
        );
        
        return AuthErrorStatisticsResponse.builder()
                .totalErrors(totalErrors)
                .errorTypeCounts(errorTypeCounts)
                .dailyCounts(dailyCounts)
                .suspiciousIps(topSuspiciousIps)
                .build();
    }
    
    /**
     * Delete an authentication error log (Level 0 only)
     */
    @Transactional
    public void deleteLog(Long adminId, Long logId, HttpServletRequest request) {
        log.info("Admin {} deleting auth error log: {}", adminId, logId);
        
        AuthenticationErrorLog errorLog = authErrorLogRepository.findById(logId)
                .orElseThrow(() -> new ResourceNotFoundException("Authentication error log not found with ID: " + logId));
        
        authErrorLogRepository.delete(errorLog);
        
        // Log admin activity (DELETE operation)
        adminActivityLogger.logDelete(
                adminId,
                "AUTH_ERROR_LOG",
                logId.toString(),
                request
        );
        
        log.info("Auth error log {} deleted successfully by admin {}", logId, adminId);
    }
}
