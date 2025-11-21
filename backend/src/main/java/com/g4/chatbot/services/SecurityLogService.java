package com.g4.chatbot.services;

import com.g4.chatbot.config.SystemPromptConfig;
import com.g4.chatbot.models.PromptInjectionLog;
import com.g4.chatbot.repos.PromptInjectionLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service for logging security-related events, particularly prompt injection attempts
 */
@Service
@Slf4j
public class SecurityLogService {
    
    @Autowired
    private SystemPromptConfig systemPromptConfig;
    
    @Autowired
    private PromptInjectionLogRepository promptInjectionLogRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Value("${app.security.alert-emails:agardan.sefa@gmail.com}")
    private String alertEmails;
    
    @Value("${app.security.send-email-alerts:true}")
    private boolean sendEmailAlerts;
    
    @Value("${app.security.email-alert-threshold:3}")
    private int emailAlertThreshold;
    
    private final ConcurrentHashMap<Long, AtomicInteger> userInjectionAttempts = new ConcurrentHashMap<>();
    
    /**
     * Log a prompt injection attempt asynchronously
     */
    @Async
    public void logPromptInjectionAttempt(Long userId, String detectedPattern, String message, String ipAddress, String userAgent) {
        logPromptInjectionAttempt(userId, detectedPattern, message, ipAddress, userAgent, null);
    }
    
    /**
     * Log a prompt injection attempt asynchronously with endpoint info
     */
    @Async
    public void logPromptInjectionAttempt(Long userId, String detectedPattern, String message, String ipAddress, String userAgent, String endpoint) {
        if (!systemPromptConfig.isLogInjectionAttempts()) {
            return;
        }
        
        try {
            // Increment user's injection attempt counter
            AtomicInteger attempts = userInjectionAttempts.computeIfAbsent(userId, k -> new AtomicInteger(0));
            int currentAttempts = attempts.incrementAndGet();
            
            // Determine severity based on attempt count
            PromptInjectionLog.Severity severity = determineSeverity(currentAttempts);
            
            // Save to database in a separate thread
            saveToDatabase(userId, detectedPattern, message, ipAddress, userAgent, endpoint, severity, currentAttempts);
            
            // Log to console
            logToConsole(userId, detectedPattern, message, ipAddress, userAgent, currentAttempts);
            
            // Send email alert if threshold reached
            if (sendEmailAlerts && currentAttempts >= emailAlertThreshold) {
                sendEmailAlert(userId, detectedPattern, message, ipAddress, currentAttempts, severity);
            }
            
        } catch (Exception e) {
            log.error("Error logging prompt injection attempt: ", e);
        }
    }
    
    /**
     * Save injection attempt to database
     */
    @Async
    protected void saveToDatabase(Long userId, String detectedPattern, String message, 
                                   String ipAddress, String userAgent, String endpoint,
                                   PromptInjectionLog.Severity severity, int attemptCount) {
        try {
            PromptInjectionLog injectionLog = PromptInjectionLog.builder()
                    .userId(userId)
                    .detectedPattern(detectedPattern)
                    .userMessage(message)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .endpoint(endpoint)
                    .severity(severity)
                    .attemptCount(attemptCount)
                    .blocked(true)
                    .emailSent(false)
                    .build();
            
            promptInjectionLogRepository.save(injectionLog);
            log.debug("Saved injection attempt to database: ID {}", injectionLog.getId());
            
        } catch (Exception e) {
            log.error("Failed to save injection attempt to database: ", e);
        }
    }
    
    /**
     * Send email alert to admin(s)
     */
    @Async
    protected void sendEmailAlert(Long userId, String detectedPattern, String message, 
                                   String ipAddress, int attemptCount, PromptInjectionLog.Severity severity) {
        try {
            String[] emails = alertEmails.split(",");
            
            String subject = String.format("🚨 SECURITY ALERT: Prompt Injection Attempt #%d (User %d)", 
                    attemptCount, userId);
            
            String body = buildEmailBody(userId, detectedPattern, message, ipAddress, attemptCount, severity);
            
            for (String email : emails) {
                emailService.sendSystemNotificationEmail(email.trim(), subject, body);
            }
            
            // Update email_sent flag in database
            updateEmailSentFlag(userId, detectedPattern);
            
            log.info("Sent email alert for injection attempt by user {} to {} recipient(s)", userId, emails.length);
            
        } catch (Exception e) {
            log.error("Failed to send email alert: ", e);
        }
    }
    
    /**
     * Update email_sent flag for the latest log
     */
    private void updateEmailSentFlag(Long userId, String detectedPattern) {
        try {
            // This is a simple approach - in production you might want to pass the log ID
            // For now, we just log that email was sent
            log.debug("Email sent flag would be updated for user {} pattern {}", userId, detectedPattern);
        } catch (Exception e) {
            log.error("Failed to update email_sent flag: ", e);
        }
    }
    
    /**
     * Build email body for security alert
     */
    private String buildEmailBody(Long userId, String detectedPattern, String message, 
                                   String ipAddress, int attemptCount, PromptInjectionLog.Severity severity) {
        return String.format("""
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6;">
                    <div style="background-color: #f8d7da; border: 1px solid #f5c6cb; border-radius: 4px; padding: 20px; margin: 20px 0;">
                        <h2 style="color: #721c24; margin-top: 0;">🚨 Prompt Injection Attempt Detected</h2>
                        
                        <table style="width: 100%%; border-collapse: collapse;">
                            <tr>
                                <td style="padding: 8px; font-weight: bold; width: 200px;">Severity:</td>
                                <td style="padding: 8px; color: %s;">%s</td>
                            </tr>
                            <tr style="background-color: #f2f2f2;">
                                <td style="padding: 8px; font-weight: bold;">User ID:</td>
                                <td style="padding: 8px;">%d</td>
                            </tr>
                            <tr>
                                <td style="padding: 8px; font-weight: bold;">Attempt Count:</td>
                                <td style="padding: 8px;">%d</td>
                            </tr>
                            <tr style="background-color: #f2f2f2;">
                                <td style="padding: 8px; font-weight: bold;">IP Address:</td>
                                <td style="padding: 8px;">%s</td>
                            </tr>
                            <tr>
                                <td style="padding: 8px; font-weight: bold;">Detected Pattern:</td>
                                <td style="padding: 8px;">%s</td>
                            </tr>
                            <tr style="background-color: #f2f2f2;">
                                <td style="padding: 8px; font-weight: bold;">Timestamp:</td>
                                <td style="padding: 8px;">%s</td>
                            </tr>
                        </table>
                        
                        <div style="margin-top: 20px; padding: 15px; background-color: #fff3cd; border-radius: 4px;">
                            <h3 style="margin-top: 0; color: #856404;">Blocked Message Preview:</h3>
                            <pre style="white-space: pre-wrap; word-wrap: break-word; background-color: #f8f9fa; padding: 10px; border-radius: 4px; overflow-x: auto;">%s</pre>
                        </div>
                        
                        <div style="margin-top: 20px; padding: 15px; background-color: #d1ecf1; border-radius: 4px;">
                            <h3 style="margin-top: 0; color: #0c5460;">Recommended Actions:</h3>
                            <ul>
                                <li>Review user activity in admin panel</li>
                                <li>Check for patterns of abuse</li>
                                <li>Consider flagging or temporarily suspending user if attempts continue</li>
                                <li>Monitor for coordinated attacks from multiple accounts</li>
                            </ul>
                        </div>
                        
                        <p style="margin-top: 20px; color: #666; font-size: 12px;">
                            This is an automated security alert from the Chatbot MCP Server.
                            <br>Do not reply to this email.
                        </p>
                    </div>
                </body>
                </html>
                """,
                getSeverityColor(severity),
                severity.name(),
                userId,
                attemptCount,
                ipAddress != null ? ipAddress : "Unknown",
                detectedPattern,
                LocalDateTime.now(),
                truncate(message, 500)
        );
    }
    
    /**
     * Get color for severity level
     */
    private String getSeverityColor(PromptInjectionLog.Severity severity) {
        return switch (severity) {
            case LOW -> "#28a745";
            case MEDIUM -> "#ffc107";
            case HIGH -> "#fd7e14";
            case CRITICAL -> "#dc3545";
        };
    }
    
    /**
     * Determine severity based on attempt count
     */
    private PromptInjectionLog.Severity determineSeverity(int attemptCount) {
        if (attemptCount == 1) {
            return PromptInjectionLog.Severity.LOW;
        } else if (attemptCount == 2) {
            return PromptInjectionLog.Severity.MEDIUM;
        } else if (attemptCount == 3) {
            return PromptInjectionLog.Severity.HIGH;
        } else {
            return PromptInjectionLog.Severity.CRITICAL;
        }
    }
    
    /**
     * Log to console
     */
    private void logToConsole(Long userId, String detectedPattern, String message, 
                              String ipAddress, String userAgent, int currentAttempts) {
        log.warn("╔════════════════════════════════════════════════════════════════");
        log.warn("║ SECURITY ALERT: Prompt Injection Attempt Detected");
        log.warn("╠════════════════════════════════════════════════════════════════");
        log.warn("║ User ID:          {}", userId);
        log.warn("║ IP Address:       {}", ipAddress != null ? ipAddress : "Unknown");
        log.warn("║ User Agent:       {}", userAgent != null ? truncate(userAgent, 60) : "Unknown");
        log.warn("║ Detected Pattern: {}", detectedPattern);
        log.warn("║ Attempt Count:    {} (for this user)", currentAttempts);
        log.warn("║ Timestamp:        {}", LocalDateTime.now());
        log.warn("╠════════════════════════════════════════════════════════════════");
        log.warn("║ Message Preview:  {}", truncate(message, 200));
        log.warn("╚════════════════════════════════════════════════════════════════");
        
        // Alert if user has multiple attempts
        if (currentAttempts >= 3) {
            log.error("⚠️  ALERT: User {} has {} injection attempts! Consider flagging/blocking.", userId, currentAttempts);
        }
    }
    
    /**
     * Get injection attempt count for a user
     */
    public int getInjectionAttemptCount(Long userId) {
        AtomicInteger attempts = userInjectionAttempts.get(userId);
        return attempts != null ? attempts.get() : 0;
    }
    
    /**
     * Reset injection attempt count for a user
     */
    public void resetInjectionAttemptCount(Long userId) {
        userInjectionAttempts.remove(userId);
        log.info("Reset injection attempt count for user {}", userId);
    }
    
    /**
     * Clear all injection attempt counts (e.g., daily cleanup)
     */
    public void clearAllInjectionAttempts() {
        int count = userInjectionAttempts.size();
        userInjectionAttempts.clear();
        log.info("Cleared injection attempt counts for {} users", count);
    }
    
    /**
     * Truncate string for logging
     */
    private String truncate(String str, int maxLength) {
        if (str == null) {
            return "null";
        }
        return str.length() > maxLength ? str.substring(0, maxLength) + "..." : str;
    }
}
