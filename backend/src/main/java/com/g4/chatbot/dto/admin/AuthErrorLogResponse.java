package com.g4.chatbot.dto.admin;

import com.g4.chatbot.models.AuthenticationErrorLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for authentication error log responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthErrorLogResponse {
    
    private Long id;
    private String errorType;
    private String errorTypeDescription;
    private Long userId;
    private String username;
    private String userEmail;
    private String ipAddress;
    private String userAgent;
    private String endpoint;
    private String httpMethod;
    private String errorMessage;
    private String attemptedAction;
    private LocalDateTime createdAt;
    
    /**
     * Convert entity to DTO
     */
    public static AuthErrorLogResponse from(AuthenticationErrorLog log) {
        return AuthErrorLogResponse.builder()
                .id(log.getId())
                .errorType(log.getErrorType().name())
                .errorTypeDescription(log.getErrorType().getDescription())
                .userId(log.getUserId())
                .username(log.getUsername())
                .userEmail(log.getUser() != null ? log.getUser().getEmail() : null)
                .ipAddress(log.getIpAddress())
                .userAgent(log.getUserAgent())
                .endpoint(log.getEndpoint())
                .httpMethod(log.getHttpMethod())
                .errorMessage(log.getErrorMessage())
                .attemptedAction(log.getAttemptedAction())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
