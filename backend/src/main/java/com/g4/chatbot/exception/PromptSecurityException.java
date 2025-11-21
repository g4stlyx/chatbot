package com.g4.chatbot.exception;

/**
 * Exception thrown when a prompt injection attempt is detected
 */
public class PromptSecurityException extends RuntimeException {
    
    private final String detectedPattern;
    private final String userMessage;
    
    public PromptSecurityException(String message) {
        super(message);
        this.detectedPattern = null;
        this.userMessage = null;
    }
    
    public PromptSecurityException(String message, String detectedPattern, String userMessage) {
        super(message);
        this.detectedPattern = detectedPattern;
        this.userMessage = userMessage;
    }
    
    public String getDetectedPattern() {
        return detectedPattern;
    }
    
    public String getUserMessage() {
        return userMessage;
    }
}
