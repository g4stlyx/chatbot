package com.g4.chatbot.dto.messages;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegenerateRequest {
    
    // Optional: specify a different model for regeneration
    private String model;
    
    // Optional: regenerate from a specific message ID (instead of last message)
    private Long fromMessageId;
}
