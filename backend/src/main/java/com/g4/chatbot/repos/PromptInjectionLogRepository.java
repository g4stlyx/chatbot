package com.g4.chatbot.repos;

import com.g4.chatbot.models.PromptInjectionLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PromptInjectionLogRepository extends JpaRepository<PromptInjectionLog, Long> {
    
    /**
     * Find all logs with pagination
     */
    Page<PromptInjectionLog> findAll(Pageable pageable);
    
    /**
     * Find logs by user ID
     */
    Page<PromptInjectionLog> findByUserId(Long userId, Pageable pageable);
    
    /**
     * Find logs by severity
     */
    Page<PromptInjectionLog> findBySeverity(PromptInjectionLog.Severity severity, Pageable pageable);
    
    /**
     * Find logs by user and severity
     */
    Page<PromptInjectionLog> findByUserIdAndSeverity(Long userId, PromptInjectionLog.Severity severity, Pageable pageable);
    
    /**
     * Count total injection attempts
     */
    long count();
    
    /**
     * Count injection attempts by user
     */
    long countByUserId(Long userId);
    
    /**
     * Count injection attempts by severity
     */
    long countBySeverity(PromptInjectionLog.Severity severity);
    
    /**
     * Find recent logs (last 24 hours)
     */
    @Query("SELECT p FROM PromptInjectionLog p WHERE p.createdAt >= :since ORDER BY p.createdAt DESC")
    List<PromptInjectionLog> findRecentLogs(@Param("since") LocalDateTime since);
    
    /**
     * Find logs by date range
     */
    @Query("SELECT p FROM PromptInjectionLog p WHERE p.createdAt BETWEEN :startDate AND :endDate ORDER BY p.createdAt DESC")
    Page<PromptInjectionLog> findByDateRange(
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate, 
        Pageable pageable
    );
    
    /**
     * Find top offenders (users with most attempts)
     */
    @Query("SELECT p.userId, COUNT(p) as attemptCount FROM PromptInjectionLog p GROUP BY p.userId ORDER BY attemptCount DESC")
    List<Object[]> findTopOffenders(Pageable pageable);
}
