package com.g4.chatbot.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity for logging prompt injection attempts
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "prompt_injection_logs", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_created_at", columnList = "created_at"),
    @Index(name = "idx_severity", columnList = "severity")
})
public class PromptInjectionLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "detected_pattern", nullable = false, length = 100)
    private String detectedPattern;
    
    @Column(name = "user_message", nullable = false, columnDefinition = "TEXT")
    private String userMessage;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Lob
    @Column(name = "user_agent")
    private String userAgent;
    
    @Column(name = "endpoint", length = 200)
    private String endpoint;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 20)
    @Builder.Default
    private Severity severity = Severity.MEDIUM;
    
    @Column(name = "attempt_count")
    private Integer attemptCount;
    
    @Column(name = "blocked", nullable = false)
    @Builder.Default
    private Boolean blocked = true;
    
    @Column(name = "email_sent", nullable = false)
    @Builder.Default
    private Boolean emailSent = false;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Reference to User entity
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public enum Severity {
        LOW,      // 1st attempt
        MEDIUM,   // 2nd attempt
        HIGH,     // 3rd attempt
        CRITICAL  // 4+ attempts
    }
}
