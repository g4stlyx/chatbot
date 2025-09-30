package com.g4.chatbot.repos;

import com.g4.chatbot.models.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    List<Message> findBySessionIdOrderByTimestampAsc(String sessionId);
    
    Page<Message> findBySessionIdOrderByTimestampAsc(String sessionId, Pageable pageable);
    
    @Query("SELECT m FROM Message m WHERE m.sessionId = :sessionId AND m.timestamp >= :since ORDER BY m.timestamp ASC")
    List<Message> findBySessionIdAndTimestampAfter(@Param("sessionId") String sessionId, @Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(m) FROM Message m WHERE m.sessionId = :sessionId")
    long countBySessionId(@Param("sessionId") String sessionId);
    
    @Query("SELECT SUM(m.tokenCount) FROM Message m WHERE m.sessionId = :sessionId")
    Long sumTokenCountBySessionId(@Param("sessionId") String sessionId);
    
    @Query("SELECT COUNT(m) FROM Message m WHERE m.timestamp >= :date")
    long countMessagesSince(@Param("date") LocalDateTime date);
    
    @Query("SELECT AVG(LENGTH(m.content)) FROM Message m WHERE m.role = 'USER'")
    Double getAverageUserMessageLength();
    
    @Query("SELECT AVG(LENGTH(m.content)) FROM Message m WHERE m.role = 'ASSISTANT'")
    Double getAverageAssistantMessageLength();
    
    // Admin queries
    @Query("SELECT m FROM Message m ORDER BY m.timestamp DESC")
    Page<Message> findAllOrderByTimestampDesc(Pageable pageable);
    
    @Query("SELECT m FROM Message m WHERE m.role = :role ORDER BY m.timestamp DESC")
    Page<Message> findByRoleOrderByTimestampDesc(@Param("role") Message.MessageRole role, Pageable pageable);
    
    @Query("SELECT m FROM Message m WHERE m.content LIKE CONCAT('%', :searchTerm, '%') ORDER BY m.timestamp DESC")
    Page<Message> findByContentContainingIgnoreCaseOrderByTimestampDesc(@Param("searchTerm") String searchTerm, Pageable pageable);
}