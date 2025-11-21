package com.g4.chatbot.dto.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO to hold security context information for requests
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityContext {
    private Long userId;
    private String ipAddress;
    private String userAgent;
    private String requestPath;
}
