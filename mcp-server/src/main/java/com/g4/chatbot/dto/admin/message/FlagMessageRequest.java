package com.g4.chatbot.dto.admin.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for flagging a message
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlagMessageRequest {
    
    @NotNull(message = "Flag type is required")
    private FlagType flagType;
    
    @NotBlank(message = "Reason is required")
    private String reason;
    
    public enum FlagType {
        INAPPROPRIATE,
        SPAM,
        HARMFUL,
        POLICY_VIOLATION,
        OTHER
    }
}
