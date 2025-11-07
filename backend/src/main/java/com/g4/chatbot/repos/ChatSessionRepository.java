package com.g4.chatbot.repos;

import com.g4.chatbot.models.ChatSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, String> {
    
    List<ChatSession> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    Page<ChatSession> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    List<ChatSession> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, ChatSession.SessionStatus status);
    
    @Query("SELECT cs FROM ChatSession cs WHERE cs.userId = :userId AND cs.status = 'ACTIVE'")
    List<ChatSession> findActiveSessionsByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(cs) FROM ChatSession cs WHERE cs.userId = :userId AND cs.status = 'ACTIVE'")
    long countActiveSessionsByUserId(@Param("userId") Long userId);
    
    @Query("SELECT cs FROM ChatSession cs WHERE cs.status = 'ACTIVE' AND cs.lastAccessedAt < :cutoffTime")
    List<ChatSession> findStaleActiveSessions(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    @Query("SELECT COUNT(cs) FROM ChatSession cs WHERE cs.createdAt >= :date")
    long countSessionsSince(@Param("date") LocalDateTime date);
    
    @Query("SELECT COUNT(cs) FROM ChatSession cs WHERE cs.status = 'ACTIVE'")
    long countActiveSessions();
    
    // Admin queries
    @Query("SELECT cs FROM ChatSession cs ORDER BY cs.createdAt DESC")
    Page<ChatSession> findAllOrderByCreatedAtDesc(Pageable pageable);
    
    @Query("SELECT cs FROM ChatSession cs WHERE cs.status = :status ORDER BY cs.createdAt DESC")
    Page<ChatSession> findByStatusOrderByCreatedAtDesc(@Param("status") ChatSession.SessionStatus status, Pageable pageable);

    // Find session by sessionId and userId for authorization
    @Query("SELECT cs FROM ChatSession cs WHERE cs.sessionId = :sessionId AND cs.userId = :userId")
    java.util.Optional<ChatSession> findBySessionIdAndUserId(@Param("sessionId") String sessionId, @Param("userId") Long userId);
    
    // Admin management queries with filtering
    Page<ChatSession> findByUserId(Long userId, Pageable pageable);
    
    Page<ChatSession> findByStatus(ChatSession.SessionStatus status, Pageable pageable);
    
    Page<ChatSession> findByIsFlagged(Boolean isFlagged, Pageable pageable);
    
    Page<ChatSession> findByIsPublic(Boolean isPublic, Pageable pageable);
    
    Page<ChatSession> findByUserIdAndStatus(Long userId, ChatSession.SessionStatus status, Pageable pageable);
    
    Page<ChatSession> findByUserIdAndIsFlagged(Long userId, Boolean isFlagged, Pageable pageable);
    
    Page<ChatSession> findByStatusAndIsFlagged(ChatSession.SessionStatus status, Boolean isFlagged, Pageable pageable);
    
    Page<ChatSession> findByUserIdAndStatusAndIsFlagged(Long userId, ChatSession.SessionStatus status, Boolean isFlagged, Pageable pageable);
}
