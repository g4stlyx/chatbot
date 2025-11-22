package com.g4.chatbot.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity for logging authentication and authorization errors (401, 403, 404)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "authentication_error_logs", indexes = {
    @Index(name = "idx_error_type", columnList = "error_type"),
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_created_at", columnList = "created_at"),
    @Index(name = "idx_ip_address", columnList = "ip_address")
})
public class AuthenticationErrorLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "error_type", nullable = false, length = 30)
    private ErrorType errorType;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "username", length = 100)
    private String username;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Lob
    @Column(name = "user_agent")
    private String userAgent;
    
    @Column(name = "endpoint", nullable = false, length = 500)
    private String endpoint;
    
    @Column(name = "http_method", length = 10)
    private String httpMethod;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "attempted_action", length = 200)
    private String attemptedAction;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Reference to User entity (if user was identified from token)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public enum ErrorType {
        UNAUTHORIZED_401("Unauthorized - Authentication Required"),
        FORBIDDEN_403("Forbidden - Insufficient Permissions"),
        NOT_FOUND_404("Resource Not Found"),
        INVALID_TOKEN("Invalid or Expired Token"),
        ACCESS_DENIED("Access Denied");
        
        private final String description;
        
        ErrorType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}
