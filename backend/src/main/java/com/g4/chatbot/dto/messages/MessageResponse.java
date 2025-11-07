package com.g4.chatbot.dto.messages;

import com.g4.chatbot.models.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private Long id;
    private String sessionId;
    private String role;
    private String content;
    private Integer tokenCount;
    private String model;
    private LocalDateTime timestamp;
    
    public static MessageResponse from(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .sessionId(message.getSessionId())
                .role(message.getRole().name().toLowerCase(java.util.Locale.ENGLISH))
                .content(message.getContent())
                .tokenCount(message.getTokenCount())
                .model(message.getModel())
                .timestamp(message.getTimestamp())
                .build();
    }
}
