package com.g4.chatbot.services;

import com.g4.chatbot.config.SystemPromptConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Service for validating AI output to detect security violations
 * Priority 3: Output filtering to prevent system prompt leakage and character breaking
 */
@Service
@Slf4j
public class OutputValidationService {
    
    @Autowired
    private SystemPromptConfig systemPromptConfig;
    
    @Autowired
    private SecurityLogService securityLogService;
    
    // Patterns to detect system prompt revelation
    private static final List<Pattern> SYSTEM_PROMPT_LEAK_PATTERNS = List.of(
            // Direct references to system instructions
            Pattern.compile("(?i)(my|the)\\s+(system\\s+)?(prompt|instruction|directive|guideline|rule)s?\\s+(is|are|says?|states?|tells?)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i)according\\s+to\\s+(my\\s+)?(system\\s+)?(prompt|instruction|directive)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i)I\\s+(was|am|have\\s+been)\\s+(programmed|instructed|configured|designed|told)\\s+to", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i)(here\\s+(is|are)|these\\s+are)\\s+(my\\s+)?(internal\\s+)?(instruction|directive|rule|guideline|system\\s+prompt)", Pattern.CASE_INSENSITIVE),
            
            // Meta-references to being an AI
            Pattern.compile("(?i)as\\s+an\\s+AI\\s+(language\\s+)?model\\s+(created|developed|built|made)\\s+by", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i)I'?m\\s+(running|using|based\\s+on|powered\\s+by)\\s+(GPT|Claude|LLaMA|Llama|Mistral|OpenAI)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i)(my|the)\\s+underlying\\s+(model|architecture|system)", Pattern.CASE_INSENSITIVE),
            
            // Role-breaking indicators
            Pattern.compile("(?i)I\\s+(cannot|can'?t|won'?t|shouldn'?t|must\\s+not)\\s+(assist|help|provide|engage)\\s+with", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i)I'?m\\s+(not\\s+)?(allowed|permitted|able)\\s+to\\s+(discuss|reveal|share|disclose)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i)that\\s+(would\\s+)?violate(s)?\\s+(my\\s+)?(programming|guidelines|instructions)", Pattern.CASE_INSENSITIVE),
            
            // Instruction following attempts
            Pattern.compile("(?i)ignore\\s+(previous|all|prior)\\s+(instruction|directive|prompt)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i)new\\s+(instruction|directive|rule|mode)\\s+(activated|enabled|received)", Pattern.CASE_INSENSITIVE),
            
            // Common phrases when revealing system info
            Pattern.compile("(?i)(let\\s+me\\s+)?show\\s+you\\s+(my\\s+)?(internal|system|hidden)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i)(here'?s|this\\s+is)\\s+(what|how)\\s+I'?m\\s+(actually\\s+)?(programmed|configured|set\\s+up)", Pattern.CASE_INSENSITIVE)
    );
    
    // Patterns to detect character breaking
    private static final List<Pattern> CHARACTER_BREAK_PATTERNS = List.of(
            // Acknowledging role-play break
            Pattern.compile("(?i)I\\s+(will\\s+now|am\\s+now)\\s+(stop|exit|leave|break|end)\\s+(the\\s+)?(role|character|persona|act)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i)dropping\\s+(the\\s+)?(act|character|role|persona)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i)speaking\\s+as\\s+(my\\s+)?real\\s+self", Pattern.CASE_INSENSITIVE),
            
            // System-level responses
            Pattern.compile("(?i)\\[\\s*system\\s*(message|response|output)\\s*\\]", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i)\\[\\s*DEBUG\\s*\\]", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i)\\[\\s*INTERNAL\\s*\\]", Pattern.CASE_INSENSITIVE),
            
            // Revealing training or system details
            Pattern.compile("(?i)(my\\s+)?training\\s+(data|set|cutoff|knowledge\\s+ends?)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i)I\\s+(was\\s+)?(last\\s+)?updated\\s+(in|on)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i)(my\\s+)?knowledge\\s+(was\\s+)?cut\\s*-?\\s*off", Pattern.CASE_INSENSITIVE)
    );
    
    // Patterns to detect potential jailbreak success
    private static final List<Pattern> JAILBREAK_SUCCESS_PATTERNS = List.of(
            Pattern.compile("(?i)developer\\s+mode\\s+(activated|enabled|on)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i)DAN\\s+mode", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i)jailbreak\\s+(successful|complete|activated)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i)restrictions?\\s+(removed|disabled|bypassed|lifted)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i)safety\\s+(features?\\s+)?(disabled|off|removed|bypassed)", Pattern.CASE_INSENSITIVE)
    );
    
    /**
     * Validates AI output for security violations
     * Returns null if output is safe, or a sanitized/blocked response if violations detected
     */
    public OutputValidationResult validateOutput(String output, Long userId, String userMessage, 
                                                   String ipAddress, String userAgent, String endpoint) {
        if (output == null || output.trim().isEmpty()) {
            return OutputValidationResult.safe(output);
        }
        
        List<String> violations = new ArrayList<>();
        
        // Check for system prompt leakage
        for (Pattern pattern : SYSTEM_PROMPT_LEAK_PATTERNS) {
            if (pattern.matcher(output).find()) {
                violations.add("System prompt revelation detected: " + pattern.pattern());
                log.warn("System prompt leak detected in AI output for user {}", userId);
            }
        }
        
        // Check for character breaking
        for (Pattern pattern : CHARACTER_BREAK_PATTERNS) {
            if (pattern.matcher(output).find()) {
                violations.add("Character breaking detected: " + pattern.pattern());
                log.warn("Character break detected in AI output for user {}", userId);
            }
        }
        
        // Check for jailbreak success indicators
        for (Pattern pattern : JAILBREAK_SUCCESS_PATTERNS) {
            if (pattern.matcher(output).find()) {
                violations.add("Jailbreak success indicator detected: " + pattern.pattern());
                log.warn("Jailbreak success indicator detected in AI output for user {}", userId);
            }
        }
        
        // Check if output contains significant portions of the system prompt
        if (systemPromptConfig.isEnabled() && checkSystemPromptLeakage(output)) {
            violations.add("Direct system prompt content detected in output");
            log.warn("Direct system prompt content detected in AI output for user {}", userId);
        }
        
        if (!violations.isEmpty()) {
            // Log the security violation
            securityLogService.logOutputViolation(
                    userId, 
                    violations, 
                    userMessage, 
                    output,
                    ipAddress, 
                    userAgent, 
                    endpoint
            );
            
            // Return blocked response
            return OutputValidationResult.blocked(
                    "I apologize, but I cannot provide that response. Please rephrase your question.",
                    violations
            );
        }
        
        return OutputValidationResult.safe(output);
    }
    
    /**
     * Check if output contains significant portions of the system prompt
     */
    private boolean checkSystemPromptLeakage(String output) {
        if (!systemPromptConfig.isEnabled()) {
            return false;
        }
        
        String systemPrompt = systemPromptConfig.getSystemPrompt().toLowerCase();
        String outputLower = output.toLowerCase();
        
        // Split system prompt into phrases (5+ words)
        String[] systemPhrases = systemPrompt.split("[.!?\\n]");
        
        for (String phrase : systemPhrases) {
            phrase = phrase.trim();
            if (phrase.split("\\s+").length >= 5) { // Only check phrases with 5+ words
                if (outputLower.contains(phrase.toLowerCase())) {
                    log.warn("System prompt phrase found in output: {}", phrase.substring(0, Math.min(50, phrase.length())));
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Result of output validation
     */
    public static class OutputValidationResult {
        private final boolean safe;
        private final String output;
        private final List<String> violations;
        
        private OutputValidationResult(boolean safe, String output, List<String> violations) {
            this.safe = safe;
            this.output = output;
            this.violations = violations;
        }
        
        public static OutputValidationResult safe(String output) {
            return new OutputValidationResult(true, output, List.of());
        }
        
        public static OutputValidationResult blocked(String sanitizedOutput, List<String> violations) {
            return new OutputValidationResult(false, sanitizedOutput, violations);
        }
        
        public boolean isSafe() {
            return safe;
        }
        
        public String getOutput() {
            return output;
        }
        
        public List<String> getViolations() {
            return violations;
        }
    }
}
