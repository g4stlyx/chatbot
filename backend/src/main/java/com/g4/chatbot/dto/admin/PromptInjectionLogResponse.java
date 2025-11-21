package com.g4.chatbot.dto.admin;

import com.g4.chatbot.models.PromptInjectionLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for prompt injection log responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptInjectionLogResponse {
    
    private Long id;
    private Long userId;
    private String username;
    private String userEmail;
    private String detectedPattern;
    private String userMessage;
    private String ipAddress;
    private String userAgent;
    private String endpoint;
    private String severity;
    private Integer attemptCount;
    private Boolean blocked;
    private Boolean emailSent;
    private LocalDateTime createdAt;
    
    /**
     * Convert entity to DTO
     */
    public static PromptInjectionLogResponse from(PromptInjectionLog log) {
        return PromptInjectionLogResponse.builder()
                .id(log.getId())
                .userId(log.getUserId())
                .username(log.getUser() != null ? log.getUser().getUsername() : null)
                .userEmail(log.getUser() != null ? log.getUser().getEmail() : null)
                .detectedPattern(log.getDetectedPattern())
                .userMessage(log.getUserMessage())
                .ipAddress(log.getIpAddress())
                .userAgent(log.getUserAgent())
                .endpoint(log.getEndpoint())
                .severity(log.getSeverity().name())
                .attemptCount(log.getAttemptCount())
                .blocked(log.getBlocked())
                .emailSent(log.getEmailSent())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
