package com.g4.chatbot.dto.session;

import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSessionRequest {
    
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;
    
    @Size(max = 50, message = "Model must not exceed 50 characters")
    private String model = "gpt-3.5-turbo";
    
    private Boolean isPublic = false;
}
