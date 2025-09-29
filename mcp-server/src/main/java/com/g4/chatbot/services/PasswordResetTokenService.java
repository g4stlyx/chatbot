package com.g4.chatbot.services;

import com.g4.chatbot.dto.PasswordResetTokenResponse;
import com.g4.chatbot.models.PasswordResetToken;
import com.g4.chatbot.repos.PasswordResetTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PasswordResetTokenService {

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    /**
     * Get all password reset tokens
     * @return List of PasswordResetTokenResponse
     */
    public List<PasswordResetTokenResponse> getAllPasswordResetTokens() {
        List<PasswordResetToken> tokens = passwordResetTokenRepository.findAll();
        return tokens.stream()
                .map(PasswordResetTokenResponse::new)
                .collect(Collectors.toList());
    }
}
