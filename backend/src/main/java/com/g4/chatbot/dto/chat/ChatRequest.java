package com.g4.chatbot.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {
    
    @NotBlank(message = "Message content cannot be empty")
    @Size(max = 10000, message = "Message content must not exceed 10000 characters")
    private String message;
    
    @Size(max = 50, message = "Model must not exceed 50 characters")
    private String model; // Optional: override session model
    
    private String sessionId; // Optional: if not provided, create new session
    
    @Size(max = 255, message = "Session title must not exceed 255 characters")
    private String sessionTitle; // Optional: title for new session (if sessionId not provided)
}
