package com.g4.chatbot.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationTokenDTO {
    private Long id;
    private String token;
    private Long userId;
    private String userType;
    private String username;
    private String email;
    private LocalDateTime expiryDate;
    private LocalDateTime createdDate;
    private boolean expired;
}
