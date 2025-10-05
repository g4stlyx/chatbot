package com.g4.chatbot.services;

import com.g4.chatbot.config.OllamaConfig;
import com.g4.chatbot.dto.ollama.OllamaChatRequest;
import com.g4.chatbot.dto.ollama.OllamaChatResponse;
import com.g4.chatbot.dto.ollama.OllamaMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
@Slf4j
public class OllamaService {
    
    @Autowired
    private OllamaConfig ollamaConfig;
    
    private final WebClient webClient;
    
    public OllamaService(OllamaConfig ollamaConfig) {
        this.ollamaConfig = ollamaConfig;
        this.webClient = WebClient.builder()
                .baseUrl(ollamaConfig.getBaseUrl())
                .build();
    }
    
    /**
     * Send chat request with streaming support
     */
    public Flux<String> chatStream(String model, List<OllamaMessage> messages) {
        log.info("Sending streaming chat request to Ollama with model: {}", model);
        log.debug("Ollama streaming request - message count: {}", messages.size());
        messages.forEach(msg -> log.debug("  - {}: {} chars", msg.getRole(), msg.getContent().length()));
        
        OllamaChatRequest request = OllamaChatRequest.builder()
                .model(model != null ? model : ollamaConfig.getDefaultModel())
                .messages(messages)
                .stream(true) // Enable streaming
                .build();
        
        return webClient.post()
                .uri("/api/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToFlux(String.class)
                .doOnNext(chunk -> log.debug("Received chunk: {}", chunk))
                .doOnError(error -> log.error("Error in Ollama streaming: ", error))
                .onErrorResume(error -> {
                    log.error("Ollama streaming error: {}", error.getMessage());
                    return Flux.error(new RuntimeException("Failed to get response from Ollama: " + error.getMessage()));
                });
    }
    
    /**
     * Send chat request without streaming (get complete response)
     */
    public String chat(String model, List<OllamaMessage> messages) {
        log.info("Sending non-streaming chat request to Ollama with model: {}", model);
        log.debug("Ollama request - message count: {}", messages.size());
        messages.forEach(msg -> log.debug("  - {}: {} chars", msg.getRole(), msg.getContent().length()));
        
        OllamaChatRequest request = OllamaChatRequest.builder()
                .model(model != null ? model : ollamaConfig.getDefaultModel())
                .messages(messages)
                .stream(false) // Disable streaming
                .build();
        
        // Log full request for debugging
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            String jsonRequest = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(request);
            log.info("=== FULL OLLAMA REQUEST JSON ===");
            log.info(jsonRequest);
            log.info("=== END REQUEST ===");
        } catch (Exception e) {
            log.warn("Could not serialize request for logging", e);
        }
        
        try {
            OllamaChatResponse response = webClient.post()
                    .uri("/api/chat")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(OllamaChatResponse.class)
                    .block();
            
            if (response != null && response.getMessage() != null) {
                log.info("Received complete response from Ollama");
                return response.getMessage().getContent();
            }
            
            throw new RuntimeException("No response from Ollama");
            
        } catch (Exception e) {
            log.error("Error communicating with Ollama: ", e);
            throw new RuntimeException("Failed to get response from Ollama: " + e.getMessage());
        }
    }
    
    /**
     * Build message history from conversation
     */
    public List<OllamaMessage> buildMessageHistory(List<com.g4.chatbot.models.Message> dbMessages) {
        return dbMessages.stream()
                .map(msg -> {
                    // CRITICAL FIX: Use Locale.ENGLISH to avoid Turkish locale issue
                    // Turkish locale converts 'I' to 'ı' (dotless i) instead of 'i'
                    // This caused "ASSISTANT" to become "assıstant" instead of "assistant"
                    String roleName = msg.getRole().name().toLowerCase(java.util.Locale.ENGLISH);
                    
                    return OllamaMessage.builder()
                            .role(roleName)
                            .content(msg.getContent())
                            .build();
                })
                .toList();
    }
    
    /**
     * Check if Ollama is available
     */
    public boolean isAvailable() {
        try {
            webClient.get()
                    .uri("/api/tags")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            log.info("Ollama service is available");
            return true;
        } catch (Exception e) {
            log.warn("Ollama service is not available: {}", e.getMessage());
            return false;
        }
    }
}
