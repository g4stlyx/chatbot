package com.g4.chatbot.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "verification_tokens")
@Data
public class VerificationToken {
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

    public VerificationToken() {
        this.token = UUID.randomUUID().toString();
        this.createdDate = LocalDateTime.now();
        // Set expiry to 24 hours from now
        this.expiryDate = this.createdDate.plusHours(24);
    }
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiryDate);
    }
}