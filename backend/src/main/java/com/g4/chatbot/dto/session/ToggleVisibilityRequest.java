package com.g4.chatbot.dto.session;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ToggleVisibilityRequest {
    
    @NotNull(message = "isPublic field is required")
    private Boolean isPublic;
}
