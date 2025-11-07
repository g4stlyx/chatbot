package com.g4.chatbot.dto.messages;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMessageRequest {
    
    @NotBlank(message = "Content cannot be empty")
    private String content;
    
    // If true, regenerate the assistant's response after updating user message
    private Boolean regenerateResponse;
}
