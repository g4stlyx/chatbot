package com.g4.chatbot.dto.auth;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    @Builder.Default
    private String tokenType = "Bearer";
    private Long expiresIn; // seconds
    private UserInfo user;
    private String message;
    private boolean success;
    private Boolean requires2FA; // True when admin has 2FA enabled and needs to complete verification
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserInfo {
        private Long id;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private String profilePicture;
        private boolean isActive;
        private boolean emailVerified;
        private String userType; // "user" or "admin"
        private Integer level; // For admins
        private Boolean twoFactorEnabled; // For admins
        private LocalDateTime lastLoginAt;
    }
}