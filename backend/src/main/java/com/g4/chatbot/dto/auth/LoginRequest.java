package com.g4.chatbot.dto.auth;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    private String username; // Can be username or email
    private String password;
    private String userType; // "user" or "admin" - specifies which account type to login as
}