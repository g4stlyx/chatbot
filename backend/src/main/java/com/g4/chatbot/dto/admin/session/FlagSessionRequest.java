package com.g4.chatbot.dto.admin.session;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for flagging a session
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlagSessionRequest {
    
    @NotNull(message = "Flag type is required")
    private FlagType flagType;
    
    @NotBlank(message = "Reason is required")
    private String reason;
    
    public enum FlagType {
        INAPPROPRIATE_CONTENT,
        SPAM,
        ABUSE,
        POLICY_VIOLATION,
        OTHER
    }
}
