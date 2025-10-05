package com.g4.chatbot.dto.chat;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatResponse {
    
    private String sessionId;
    private Long userMessageId;
    private Long assistantMessageId;
    private String userMessage;
    private String assistantMessage;
    private String model;
    private Integer tokenCount;
    private LocalDateTime timestamp;
    private Boolean isNewSession; // Indicates if a new session was created
}
