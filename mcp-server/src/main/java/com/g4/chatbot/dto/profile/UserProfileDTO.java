package com.g4.chatbot.dto.profile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String profilePicture;
    private Boolean isActive;
    private Boolean emailVerified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
}
