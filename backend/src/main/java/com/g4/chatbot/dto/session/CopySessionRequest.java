package com.g4.chatbot.dto.session;

import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CopySessionRequest {
    
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String newTitle;
}
