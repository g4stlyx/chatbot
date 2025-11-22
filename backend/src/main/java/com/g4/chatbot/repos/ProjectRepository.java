package com.g4.chatbot.repos;

import com.g4.chatbot.models.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    
    /**
     * Find all projects by user ID with pagination
     */
    Page<Project> findByUserId(Long userId, Pageable pageable);
    
    /**
     * Find all projects by user ID (non-paginated)
     */
    List<Project> findByUserId(Long userId);
    
    /**
     * Find non-archived projects by user ID
     */
    Page<Project> findByUserIdAndIsArchivedFalse(Long userId, Pageable pageable);
    
    /**
     * Find archived projects by user ID
     */
    Page<Project> findByUserIdAndIsArchivedTrue(Long userId, Pageable pageable);
    
    /**
     * Find project by ID and user ID (for authorization)
     */
    Optional<Project> findByIdAndUserId(Long id, Long userId);
    
    /**
     * Search projects by name (case-insensitive)
     */
    @Query("SELECT p FROM Project p WHERE p.userId = :userId AND LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY p.updatedAt DESC")
    Page<Project> searchByName(@Param("userId") Long userId, @Param("query") String query, Pageable pageable);
    
    /**
     * Count projects by user ID
     */
    Long countByUserId(Long userId);
    
    /**
     * Count non-archived projects by user ID
     */
    Long countByUserIdAndIsArchivedFalse(Long userId);
    
    /**
     * Update session count for a project
     */
    @Modifying
    @Query("UPDATE Project p SET p.sessionCount = :count WHERE p.id = :projectId")
    void updateSessionCount(@Param("projectId") Long projectId, @Param("count") Integer count);
    
    /**
     * Increment session count
     */
    @Modifying
    @Query("UPDATE Project p SET p.sessionCount = p.sessionCount + 1 WHERE p.id = :projectId")
    void incrementSessionCount(@Param("projectId") Long projectId);
    
    /**
     * Decrement session count
     */
    @Modifying
    @Query("UPDATE Project p SET p.sessionCount = p.sessionCount - 1 WHERE p.id = :projectId AND p.sessionCount > 0")
    void decrementSessionCount(@Param("projectId") Long projectId);
    
    /**
     * Check if project name exists for user
     */
    boolean existsByUserIdAndName(Long userId, String name);
}
