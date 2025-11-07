package com.g4.chatbot.dto.ollama;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OllamaChatRequest {
    private String model;
    private List<OllamaMessage> messages;
    private Boolean stream; // Enable streaming responses
}
