package com.g4.chatbot.services;

import com.g4.chatbot.models.AuthenticationErrorLog;
import com.g4.chatbot.repos.AuthenticationErrorLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for logging authentication and authorization errors asynchronously
 */
@Service
@Slf4j
public class AuthErrorLogService {
    
    @Autowired
    private AuthenticationErrorLogRepository authErrorLogRepository;
    
    @Value("${app.security.log-auth-errors:true}")
    private boolean logAuthErrors;
    
    /**
     * Log authentication error asynchronously with separate transaction
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAuthError(
            AuthenticationErrorLog.ErrorType errorType,
            Long userId,
            String username,
            String ipAddress,
            String userAgent,
            String endpoint,
            String httpMethod,
            String errorMessage,
            String attemptedAction
    ) {
        if (!logAuthErrors) {
            return;
        }
        
        try {
            AuthenticationErrorLog errorLog = AuthenticationErrorLog.builder()
                    .errorType(errorType)
                    .userId(userId)
                    .username(username)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .endpoint(endpoint)
                    .httpMethod(httpMethod)
                    .errorMessage(errorMessage)
                    .attemptedAction(attemptedAction)
                    .build();
            
            authErrorLogRepository.save(errorLog);
            
            // Log to console with formatted output
            logToConsole(errorType, userId, username, ipAddress, endpoint, errorMessage);
            
        } catch (Exception e) {
            log.error("Failed to log authentication error: {}", e.getMessage());
            // Fail gracefully - don't let logging errors affect main application flow
        }
    }
    
    /**
     * Log 401 Unauthorized error
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log401(String ipAddress, String userAgent, String endpoint, String httpMethod, String errorMessage) {
        logAuthError(
                AuthenticationErrorLog.ErrorType.UNAUTHORIZED_401,
                null,
                null,
                ipAddress,
                userAgent,
                endpoint,
                httpMethod,
                errorMessage,
                "Authentication required"
        );
    }
    
    /**
     * Log 403 Forbidden error
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log403(Long userId, String username, String ipAddress, String userAgent, String endpoint, String httpMethod, String errorMessage, String attemptedAction) {
        logAuthError(
                AuthenticationErrorLog.ErrorType.FORBIDDEN_403,
                userId,
                username,
                ipAddress,
                userAgent,
                endpoint,
                httpMethod,
                errorMessage,
                attemptedAction
        );
    }
    
    /**
     * Log 404 Not Found error (resource access attempt)
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log404(Long userId, String username, String ipAddress, String userAgent, String endpoint, String httpMethod, String resourceType) {
        logAuthError(
                AuthenticationErrorLog.ErrorType.NOT_FOUND_404,
                userId,
                username,
                ipAddress,
                userAgent,
                endpoint,
                httpMethod,
                "Resource not found: " + resourceType,
                "Attempted to access non-existent resource"
        );
    }
    
    /**
     * Log invalid token error
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logInvalidToken(String ipAddress, String userAgent, String endpoint, String httpMethod, String tokenError) {
        logAuthError(
                AuthenticationErrorLog.ErrorType.INVALID_TOKEN,
                null,
                null,
                ipAddress,
                userAgent,
                endpoint,
                httpMethod,
                tokenError,
                "Invalid or expired authentication token"
        );
    }
    
    /**
     * Log access denied error
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAccessDenied(Long userId, String username, String ipAddress, String userAgent, String endpoint, String httpMethod, String reason) {
        logAuthError(
                AuthenticationErrorLog.ErrorType.ACCESS_DENIED,
                userId,
                username,
                ipAddress,
                userAgent,
                endpoint,
                httpMethod,
                reason,
                "Access denied to protected resource"
        );
    }
    
    /**
     * Log formatted message to console
     */
    private void logToConsole(
            AuthenticationErrorLog.ErrorType errorType,
            Long userId,
            String username,
            String ipAddress,
            String endpoint,
            String errorMessage
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n╔════════════════════════════════════════════════════════════════════════════════╗\n");
        sb.append("║ Authentication Error Detected                                                  ║\n");
        sb.append("╠════════════════════════════════════════════════════════════════════════════════╣\n");
        sb.append(String.format("║ Error Type: %-66s ║\n", errorType.name() + " - " + errorType.getDescription()));
        
        if (userId != null) {
            sb.append(String.format("║ User ID: %-69s ║\n", userId));
        }
        if (username != null && !username.isEmpty()) {
            sb.append(String.format("║ Username: %-68s ║\n", username));
        }
        if (ipAddress != null && !ipAddress.isEmpty()) {
            sb.append(String.format("║ IP Address: %-66s ║\n", ipAddress));
        }
        sb.append(String.format("║ Endpoint: %-68s ║\n", endpoint != null ? endpoint : "N/A"));
        if (errorMessage != null && !errorMessage.isEmpty()) {
            // Truncate long messages
            String msg = errorMessage.length() > 60 ? errorMessage.substring(0, 57) + "..." : errorMessage;
            sb.append(String.format("║ Message: %-69s ║\n", msg));
        }
        sb.append("╚════════════════════════════════════════════════════════════════════════════════╝");
        
        log.warn(sb.toString());
    }
}
