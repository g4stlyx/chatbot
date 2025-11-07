package com.g4.chatbot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${server.port}")
    private int serverPort;

    @Value("${app.security.pepper}")
    private String pepper;

    // AI Configuration
    @Value("${app.ai.openai.api-key:}")
    private String openaiApiKey;

    @Value("${app.ai.openai.base-url}")
    private String openaiBaseUrl;

    @Value("${app.ai.openai.default-model}")
    private String openaiDefaultModel;

    @Value("${app.ai.openai.max-tokens}")
    private int openaiMaxTokens;

    @Value("${app.ai.openai.temperature}")
    private double openaiTemperature;

    // Ollama Configuration
    @Value("${app.ai.ollama.enabled}")
    private boolean ollamaEnabled;

    @Value("${app.ai.ollama.base-url}")
    private String ollamaBaseUrl;

    @Value("${app.ai.ollama.default-model}")
    private String ollamaDefaultModel;

    // Password Configuration
    @Value("${app.security.argon2.memory-cost}")
    private int argon2MemoryCost;

    @Value("${app.security.argon2.time-cost}")
    private int argon2TimeCost;

    @Value("${app.security.argon2.parallelism}")
    private int argon2Parallelism;

    @Value("${app.security.argon2.salt-length}")
    private int argon2SaltLength;

    @Value("${app.security.argon2.hash-length}")
    private int argon2HashLength;

    // Getters
    public String getApplicationName() { return applicationName; }
    public int getServerPort() { return serverPort; }
    public String getPepper() { return pepper; }
    
    // AI Getters
    public String getOpenaiApiKey() { return openaiApiKey; }
    public String getOpenaiBaseUrl() { return openaiBaseUrl; }
    public String getOpenaiDefaultModel() { return openaiDefaultModel; }
    public int getOpenaiMaxTokens() { return openaiMaxTokens; }
    public double getOpenaiTemperature() { return openaiTemperature; }
    
    public boolean isOllamaEnabled() { return ollamaEnabled; }
    public String getOllamaBaseUrl() { return ollamaBaseUrl; }
    public String getOllamaDefaultModel() { return ollamaDefaultModel; }
    
    // Password Getters
    public int getArgon2MemoryCost() { return argon2MemoryCost; }
    public int getArgon2TimeCost() { return argon2TimeCost; }
    public int getArgon2Parallelism() { return argon2Parallelism; }
    public int getArgon2SaltLength() { return argon2SaltLength; }
    public int getArgon2HashLength() { return argon2HashLength; }
}