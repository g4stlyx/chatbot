package com.g4.chatbot.dto.admin.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for admin view of messages with full context
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminMessageDTO {
    
    private Long id;
    
    private String sessionId;
    private String sessionTitle;
    
    private Long userId;
    private String username;
    private String userEmail;
    
    private String role;
    private String content;
    
    private Integer tokenCount;
    private String model;
    
    private Boolean isFlagged;
    private String flagReason;
    
    private LocalDateTime timestamp;
}
