package com.g4.chatbot.exception;

import com.g4.chatbot.security.JwtUtils;
import com.g4.chatbot.services.AuthErrorLogService;
import com.g4.chatbot.services.SecurityLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @Autowired
    private SecurityLogService securityLogService;
    
    @Autowired
    private AuthErrorLogService authErrorLogService;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    /**
     * Handle Resource Not Found Exception (404)
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex, 
            HttpServletRequest request) {
        
        log.error("Resource not found: {}", ex.getMessage());
        
        // Log 404 error with user info if available
        UserInfo userInfo = extractUserInfo(request);
        authErrorLogService.log404(
            userInfo.userId,
            userInfo.username,
            getClientIP(request),
            request.getHeader("User-Agent"),
            request.getRequestURI(),
            request.getMethod(),
            ex.getMessage()
        );
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
    
    /**
     * Handle Unauthorized Exception (403 - Forbidden)
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(
            UnauthorizedException ex, 
            HttpServletRequest request) {
        
        log.error("Unauthorized access: {}", ex.getMessage());
        
        // Log 403 error
        UserInfo userInfo = extractUserInfo(request);
        authErrorLogService.log403(
            userInfo.userId,
            userInfo.username,
            getClientIP(request),
            request.getHeader("User-Agent"),
            request.getRequestURI(),
            request.getMethod(),
            ex.getMessage(),
            "Access to resource denied"
        );
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                ex.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }
    
    /**
     * Handle Spring Security Access Denied Exception (403)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex,
            HttpServletRequest request) {
        
        log.error("Access denied: {}", ex.getMessage());
        
        // Log 403 error
        UserInfo userInfo = extractUserInfo(request);
        authErrorLogService.log403(
            userInfo.userId,
            userInfo.username,
            getClientIP(request),
            request.getHeader("User-Agent"),
            request.getRequestURI(),
            request.getMethod(),
            ex.getMessage(),
            "Insufficient permissions"
        );
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                "Access denied. You do not have permission to access this resource.",
                request.getRequestURI(),
                LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }
    
    /**
     * Handle Spring Security Authentication Exception (401)
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex,
            HttpServletRequest request) {
        
        log.error("Authentication failed: {}", ex.getMessage());
        
        // Log 401 error
        authErrorLogService.log401(
            getClientIP(request),
            request.getHeader("User-Agent"),
            request.getRequestURI(),
            request.getMethod(),
            ex.getMessage()
        );
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "Authentication is required to access this resource.",
                request.getRequestURI(),
                LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
    
    /**
     * Handle Bad Request Exception
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(
            BadRequestException ex, 
            HttpServletRequest request) {
        
        log.error("Bad request: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    /**
     * Handle Prompt Security Exception (Prompt Injection Attempts)
     */
    @ExceptionHandler(PromptSecurityException.class)
    public ResponseEntity<ErrorResponse> handlePromptSecurityException(
            PromptSecurityException ex, 
            HttpServletRequest request) {
        
        // Extract user ID from security context
        Long userId = getCurrentUserId();
        
        // Log the security incident asynchronously
        securityLogService.logPromptInjectionAttempt(
            userId,
            ex.getDetectedPattern(),
            ex.getUserMessage(),
            getClientIP(request),
            request.getHeader("User-Agent")
        );
        
        log.warn("SECURITY: Prompt injection attempt detected - Pattern: {}, Path: {}, IP: {}, User: {}", 
                ex.getDetectedPattern(),
                request.getRequestURI(),
                getClientIP(request),
                userId);
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Security Violation",
                ex.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    /**
     * Handle Validation Exceptions
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validation Failed");
        response.put("errors", errors);
        response.put("path", request.getRequestURI());
        response.put("timestamp", LocalDateTime.now());
        
        log.error("Validation failed: {}", errors);
        
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * Handle Generic Exceptions (500 errors)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, 
            HttpServletRequest request) {
        
        log.error("Internal server error: ", ex);
        
        // Log 500 error
        try {
            UserInfo userInfo = extractUserInfo(request);
            authErrorLogService.logAuthError(
                com.g4.chatbot.models.AuthenticationErrorLog.ErrorType.ACCESS_DENIED,
                userInfo.userId,
                userInfo.username,
                getClientIP(request),
                request.getHeader("User-Agent"),
                request.getRequestURI(),
                request.getMethod(),
                "Internal Server Error: " + ex.getClass().getSimpleName() + " - " + ex.getMessage(),
                "500 Internal Server Error"
            );
        } catch (Exception logEx) {
            log.error("Failed to log 500 error: {}", logEx.getMessage());
        }
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred. Please try again later.",
                request.getRequestURI(),
                LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    
    /**
     * Helper method to get client IP address
     */
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
    
    /**
     * Helper method to get current user ID from security context
     */
    private Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getDetails() instanceof Long) {
                return (Long) authentication.getDetails();
            }
        } catch (Exception e) {
            log.debug("Could not extract user ID from security context", e);
        }
        return null;
    }
    
    /**
     * Helper method to extract user info from request (JWT token + security context)
     */
    private UserInfo extractUserInfo(HttpServletRequest request) {
        UserInfo userInfo = new UserInfo();
        
        try {
            // Try to get from security context first
            userInfo.userId = getCurrentUserId();
            
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof String) {
                userInfo.username = (String) authentication.getPrincipal();
            }
            
            // Try to extract from JWT token if available
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    String token = authHeader.substring(7);
                    if (userInfo.username == null) {
                        userInfo.username = jwtUtils.extractUsername(token);
                    }
                    if (userInfo.userId == null) {
                        userInfo.userId = jwtUtils.extractUserIdAsLong(token);
                    }
                } catch (Exception e) {
                    log.debug("Could not extract user info from JWT token", e);
                }
            }
        } catch (Exception e) {
            log.debug("Error extracting user info", e);
        }
        
        return userInfo;
    }
    
    /**
     * Inner class to hold user information
     */
    private static class UserInfo {
        Long userId;
        String username;
    }
}
