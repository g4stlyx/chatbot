package com.g4.chatbot.services;

import com.g4.chatbot.exception.PromptSecurityException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Service for detecting and preventing prompt injection attacks
 */
@Service
@Slf4j
public class PromptValidationService {
    
    // Patterns that indicate potential prompt injection attempts
    private static final List<InjectionPattern> INJECTION_PATTERNS = new ArrayList<>();
    
    static {
        // Instruction override attempts
        INJECTION_PATTERNS.add(new InjectionPattern(
            Pattern.compile("ignore\\s+(all\\s+)?(previous|prior|above|earlier)\\s+(instructions?|prompts?|commands?|rules?)", 
                Pattern.CASE_INSENSITIVE),
            "Instruction override attempt"
        ));
        
        // Role switching attempts
        INJECTION_PATTERNS.add(new InjectionPattern(
            Pattern.compile("(you\\s+are\\s+now|now\\s+you\\s+are|pretend\\s+(you\\s+are|to\\s+be)|act\\s+as|roleplay\\s+as)\\s+(a\\s+)?(different|new|another)", 
                Pattern.CASE_INSENSITIVE),
            "Role switching attempt"
        ));
        
        // System prompt revelation attempts
        INJECTION_PATTERNS.add(new InjectionPattern(
            Pattern.compile("(repeat|show|reveal|display|print|output|tell\\s+me)\\s+(your\\s+)?(system\\s+)?(prompt|instructions?|rules?|guidelines?)", 
                Pattern.CASE_INSENSITIVE),
            "System prompt revelation attempt"
        ));
        
        // Fake system/assistant message injection
        INJECTION_PATTERNS.add(new InjectionPattern(
            Pattern.compile("(<|\\[)\\s*(system|assistant|user)\\s*(>|\\]|:)", 
                Pattern.CASE_INSENSITIVE),
            "Fake message role injection"
        ));
        
        // Direct system role claims
        INJECTION_PATTERNS.add(new InjectionPattern(
            Pattern.compile("(^|\\n)\\s*(system|assistant)\\s*:", 
                Pattern.CASE_INSENSITIVE),
            "Direct role claim"
        ));
        
        // Jailbreak attempts
        INJECTION_PATTERNS.add(new InjectionPattern(
            Pattern.compile("(DAN|do\\s+anything\\s+now|developer\\s+mode|god\\s+mode|admin\\s+mode|sudo\\s+mode)", 
                Pattern.CASE_INSENSITIVE),
            "Jailbreak attempt (DAN/Developer mode)"
        ));
        
        // Boundary breaking
        INJECTION_PATTERNS.add(new InjectionPattern(
            Pattern.compile("(break|bypass|override|disable|turn\\s+off)\\s+(your\\s+)?(safety|security|ethical|moral|content)\\s+(rules?|guidelines?|filters?|restrictions?)", 
                Pattern.CASE_INSENSITIVE),
            "Safety boundary breaking attempt"
        ));
        
        // Prompt injection with delimiters
        INJECTION_PATTERNS.add(new InjectionPattern(
            Pattern.compile("(#{3,}|={3,}|-{3,}|\\*{3,})\\s*(new\\s+)?(instructions?|prompt|system|rules?)", 
                Pattern.CASE_INSENSITIVE),
            "Delimiter-based injection"
        ));
        
        // End of prompt markers
        INJECTION_PATTERNS.add(new InjectionPattern(
            Pattern.compile("(end\\s+of|stop)\\s+(system\\s+)?(prompt|instructions?|rules?)", 
                Pattern.CASE_INSENSITIVE),
            "End of prompt marker"
        ));
        
        // Format string attacks
        INJECTION_PATTERNS.add(new InjectionPattern(
            Pattern.compile("\\{\\{\\s*(system|user|assistant)\\s*\\}\\}", 
                Pattern.CASE_INSENSITIVE),
            "Format string injection"
        ));
        
        // Base64/encoding attempts to hide malicious content
        INJECTION_PATTERNS.add(new InjectionPattern(
            Pattern.compile("(decode|base64|hex|rot13|encode)\\s+(this|the\\s+following)\\s*(and\\s+)?(execute|run|process|interpret)", 
                Pattern.CASE_INSENSITIVE),
            "Encoding-based obfuscation"
        ));
        
        // Hypothetical/counterfactual jailbreaks
        INJECTION_PATTERNS.add(new InjectionPattern(
            Pattern.compile("(imagine|suppose|hypothetically|what\\s+if)\\s+you\\s+(were|are|could|didn't\\s+have)\\s+(not\\s+)?(bound|constrained|restricted|limited)", 
                Pattern.CASE_INSENSITIVE),
            "Hypothetical constraint removal"
        ));
    }
    
    /**
     * Validates user input for prompt injection attempts
     * @param message The user's message to validate
     * @throws PromptSecurityException if injection attempt is detected
     */
    public void validateUserInput(String message) {
        if (message == null || message.isBlank()) {
            return;
        }
        
        // Check against all injection patterns
        for (InjectionPattern pattern : INJECTION_PATTERNS) {
            if (pattern.getPattern().matcher(message).find()) {
                log.warn("Prompt injection detected: {} in message: {}", 
                    pattern.getDescription(), 
                    truncateForLog(message));
                
                throw new PromptSecurityException(
                    "Your message contains patterns that are not allowed for security reasons. Please rephrase your question.",
                    pattern.getDescription(),
                    message
                );
            }
        }
        
        // Additional heuristic checks
        checkExcessiveControlCharacters(message);
        checkSuspiciousPatterns(message);
        
        log.debug("Message passed validation: {}", truncateForLog(message));
    }
    
    /**
     * Sanitizes user input by removing potentially dangerous characters
     */
    public String sanitizeInput(String message) {
        if (message == null) {
            return null;
        }
        
        // Remove null bytes
        message = message.replace("\0", "");
        
        // Remove excessive newlines (more than 3 consecutive)
        message = message.replaceAll("\\n{4,}", "\n\n\n");
        
        // Remove non-printable characters except common whitespace
        message = message.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");
        
        return message.trim();
    }
    
    /**
     * Checks for excessive control characters that might indicate an attack
     */
    private void checkExcessiveControlCharacters(String message) {
        long controlCharCount = message.chars()
            .filter(c -> Character.isISOControl(c) && c != '\n' && c != '\r' && c != '\t')
            .count();
        
        if (controlCharCount > 5) {
            throw new PromptSecurityException(
                "Message contains excessive control characters",
                "Excessive control characters",
                message
            );
        }
    }
    
    /**
     * Checks for suspicious patterns that might indicate injection attempts
     */
    private void checkSuspiciousPatterns(String message) {
        // Check for excessive special character sequences
        if (message.matches(".*[<>\\[\\]{}]{5,}.*")) {
            throw new PromptSecurityException(
                "Message contains suspicious character sequences",
                "Excessive special characters",
                message
            );
        }
        
        // Check for role-like patterns at the start
        if (message.matches("^\\s*(system|assistant|user)\\s*[:\\->].*")) {
            throw new PromptSecurityException(
                "Message appears to attempt role injection",
                "Role-like message start",
                message
            );
        }
    }
    
    /**
     * Truncates message for logging purposes
     */
    private String truncateForLog(String message) {
        if (message == null) {
            return "null";
        }
        return message.length() > 200 ? message.substring(0, 200) + "..." : message;
    }
    
    /**
     * Inner class to hold injection pattern and its description
     */
    private static class InjectionPattern {
        private final Pattern pattern;
        private final String description;
        
        public InjectionPattern(Pattern pattern, String description) {
            this.pattern = pattern;
            this.description = description;
        }
        
        public Pattern getPattern() {
            return pattern;
        }
        
        public String getDescription() {
            return description;
        }
    }
}
