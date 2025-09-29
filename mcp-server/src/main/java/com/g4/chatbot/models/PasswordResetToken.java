package com.g4.chatbot.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "password_reset_tokens")
@Data
public class PasswordResetToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(nullable = false, unique = true)
    private String token;
    
    @Column(name = "user_id", nullable = false)
    private Integer userId;
    
    @Column(name = "user_type", nullable = false)
    private String userType;
    
    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;
    
    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;
    
    // For password reset tokens, we might want to track attempts
    @Column(name = "attempt_count")
    private Integer attemptCount = 0;
    
    // We might want to add IP address for security
    @Column(name = "requesting_ip")
    private String requestingIp;
    
    public PasswordResetToken() {
        this.token = UUID.randomUUID().toString();
        this.createdDate = LocalDateTime.now();
        // Password reset tokens should expire quickly - 15 minutes
        this.expiryDate = this.createdDate.plusMinutes(15);
    }
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiryDate);
    }
    
    public void incrementAttemptCount() {
        this.attemptCount++;
    }
    
    public boolean hasTooManyAttempts() {
        return this.attemptCount >= 3; // Block after 3 attempts
    }
}