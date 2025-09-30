package com.g4.chatbot.repos;

import com.g4.chatbot.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByUsernameOrEmail(String username, String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE u.isActive = true")
    java.util.List<User> findAllActiveUsers();
    
    @Query("SELECT u FROM User u WHERE u.emailVerified = false")
    java.util.List<User> findAllUnverifiedUsers();
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :date")
    long countUsersSince(@Param("date") LocalDateTime date);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.lastLoginAt >= :date")
    long countActiveUsersSince(@Param("date") LocalDateTime date);
}