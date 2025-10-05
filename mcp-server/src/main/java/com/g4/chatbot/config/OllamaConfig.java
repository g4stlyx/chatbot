package com.g4.chatbot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.ai.ollama")
@Data
public class OllamaConfig {
    
    private boolean enabled = true;
    private String baseUrl = "http://localhost:11434";
    private String defaultModel = "llama3";
    private String apiKey = "ollama";
    
    public String getChatEndpoint() {
        return baseUrl + "/api/chat";
    }
    
    public String getGenerateEndpoint() {
        return baseUrl + "/api/generate";
    }
}
