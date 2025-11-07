package com.g4.chatbot.dto.ollama;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OllamaMessage {
    private String role; // "user", "assistant", or "system"
    private String content;
}
