package com.g4.chatbot.repos;

import com.g4.chatbot.models.AuthenticationErrorLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuthenticationErrorLogRepository extends JpaRepository<AuthenticationErrorLog, Long> {
    
    /**
     * Find all logs with pagination
     */
    Page<AuthenticationErrorLog> findAll(Pageable pageable);
    
    /**
     * Find logs by error type
     */
    Page<AuthenticationErrorLog> findByErrorType(AuthenticationErrorLog.ErrorType errorType, Pageable pageable);
    
    /**
     * Find logs by user ID (if user was identified)
     */
    Page<AuthenticationErrorLog> findByUserId(Long userId, Pageable pageable);
    
    /**
     * Find logs by IP address
     */
    Page<AuthenticationErrorLog> findByIpAddress(String ipAddress, Pageable pageable);
    
    /**
     * Find logs within date range
     */
    @Query("SELECT a FROM AuthenticationErrorLog a WHERE a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
    Page<AuthenticationErrorLog> findByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );
    
    /**
     * Count errors by IP address within time window (for rate limiting detection)
     */
    @Query("SELECT COUNT(a) FROM AuthenticationErrorLog a WHERE a.ipAddress = :ipAddress AND a.createdAt >= :since")
    Long countByIpAddressSince(@Param("ipAddress") String ipAddress, @Param("since") LocalDateTime since);
    
    /**
     * Count errors by user ID within time window
     */
    @Query("SELECT COUNT(a) FROM AuthenticationErrorLog a WHERE a.userId = :userId AND a.createdAt >= :since")
    Long countByUserIdSince(@Param("userId") Long userId, @Param("since") LocalDateTime since);
    
    /**
     * Get recent errors by IP address
     */
    @Query("SELECT a FROM AuthenticationErrorLog a WHERE a.ipAddress = :ipAddress ORDER BY a.createdAt DESC")
    List<AuthenticationErrorLog> findRecentByIpAddress(@Param("ipAddress") String ipAddress, Pageable pageable);
    
    /**
     * Get statistics by error type
     */
    @Query("SELECT a.errorType, COUNT(a) FROM AuthenticationErrorLog a GROUP BY a.errorType")
    List<Object[]> getStatisticsByErrorType();
    
    /**
     * Get statistics for last N days
     */
    @Query("SELECT DATE(a.createdAt) as date, COUNT(a) as count FROM AuthenticationErrorLog a " +
           "WHERE a.createdAt >= :since GROUP BY DATE(a.createdAt) ORDER BY date DESC")
    List<Object[]> getDailyStatistics(@Param("since") LocalDateTime since);
}
