package com.g4.chatbot.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.g4.chatbot.services.AuthErrorLogService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthEntryPoint.class);
    
    @Autowired
    private AuthErrorLogService authErrorLogService;
    
    @Autowired
    @org.springframework.beans.factory.annotation.Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping handlerMapping;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, 
                         AuthenticationException authException) throws IOException, ServletException {
        
        // Check if endpoint actually exists
        boolean endpointExists = true;
        try {
            Object handler = handlerMapping.getHandler(request);
            if (handler == null) {
                endpointExists = false;
            }
        } catch (Exception e) {
            // If we can't determine, assume it exists
            logger.debug("Could not determine if endpoint exists: {}", e.getMessage());
        }
        
        // If endpoint doesn't exist, return 404 instead of 401
        if (!endpointExists) {
            logger.error("Endpoint not found: {}", request.getRequestURI());
            
            // Log as 404
            try {
                authErrorLogService.log404(
                    null,
                    null,
                    getClientIP(request),
                    request.getHeader("User-Agent"),
                    request.getRequestURI(),
                    request.getMethod(),
                    "Endpoint not found"
                );
            } catch (Exception e) {
                logger.error("Failed to log 404 error: {}", e.getMessage());
            }
            
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);

            final Map<String, Object> body = new HashMap<>();
            body.put("status", HttpServletResponse.SC_NOT_FOUND);
            body.put("error", "Not Found");
            body.put("message", "The requested endpoint does not exist");
            body.put("path", request.getServletPath());
            body.put("timestamp", System.currentTimeMillis());

            final ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(response.getOutputStream(), body);
            return;
        }
        
        logger.error("Unauthorized error: {}", authException.getMessage());
        
        // Log 401 error
        try {
            authErrorLogService.log401(
                getClientIP(request),
                request.getHeader("User-Agent"),
                request.getRequestURI(),
                request.getMethod(),
                authException.getMessage()
            );
        } catch (Exception e) {
            logger.error("Failed to log 401 error: {}", e.getMessage());
        }

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        final Map<String, Object> body = new HashMap<>();
        body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        body.put("error", "Unauthorized");
        body.put("message", "Authentication required to access this resource");
        body.put("path", request.getServletPath());
        body.put("timestamp", System.currentTimeMillis());

        final ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), body);
    }
    
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}