package com.g4.chatbot.dto.admin.session;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for admin view of chat sessions with full details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminChatSessionDTO {
    
    private String sessionId;
    
    private Long userId;
    private String username;
    private String userEmail;
    
    private String title;
    private String model;
    private String status;
    
    private Integer messageCount;
    private Long tokenUsage;
    
    private Boolean isPublic;
    private Boolean isFlagged;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastAccessedAt;
    private LocalDateTime expiresAt;
}
