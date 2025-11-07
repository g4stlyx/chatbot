package com.g4.chatbot.dto.session;

import com.g4.chatbot.models.ChatSession;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionResponse {
    
    private String sessionId;
    private String title;
    private String model;
    private String status;
    private Integer messageCount;
    private Long tokenUsage;
    private Boolean isPublic;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastAccessedAt;
    private LocalDateTime expiresAt;
    
    public static SessionResponse fromEntity(ChatSession session) {
        return SessionResponse.builder()
                .sessionId(session.getSessionId())
                .title(session.getTitle())
                .model(session.getModel())
                .status(session.getStatus().name())
                .messageCount(session.getMessageCount())
                .tokenUsage(session.getTokenUsage())
                .isPublic(session.getIsPublic())
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                .lastAccessedAt(session.getLastAccessedAt())
                .expiresAt(session.getExpiresAt())
                .build();
    }
}
