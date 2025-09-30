package com.g4.chatbot.repos;

import com.g4.chatbot.models.MessageFlag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageFlagRepository extends JpaRepository<MessageFlag, Long> {
    
    List<MessageFlag> findByMessageId(Long messageId);
    
    List<MessageFlag> findByFlaggedBy(Long adminId);
    
    List<MessageFlag> findByStatus(MessageFlag.FlagStatus status);
    
    Page<MessageFlag> findByStatusOrderByCreatedAtDesc(MessageFlag.FlagStatus status, Pageable pageable);
    
    @Query("SELECT mf FROM MessageFlag mf WHERE mf.status = 'PENDING' ORDER BY mf.createdAt ASC")
    List<MessageFlag> findPendingFlags();
    
    @Query("SELECT COUNT(mf) FROM MessageFlag mf WHERE mf.status = 'PENDING'")
    long countPendingFlags();
    
    @Query("SELECT COUNT(mf) FROM MessageFlag mf WHERE mf.flaggedBy = :adminId")
    long countByFlaggedBy(@Param("adminId") Long adminId);
    
    boolean existsByMessageIdAndFlaggedBy(Long messageId, Long adminId);
}