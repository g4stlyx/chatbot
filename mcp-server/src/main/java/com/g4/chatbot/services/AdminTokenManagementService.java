package com.g4.chatbot.services;

import com.g4.chatbot.dto.admin.PasswordResetTokenDTO;
import com.g4.chatbot.dto.admin.TokenListResponse;
import com.g4.chatbot.dto.admin.VerificationTokenDTO;
import com.g4.chatbot.models.Admin;
import com.g4.chatbot.models.PasswordResetToken;
import com.g4.chatbot.models.User;
import com.g4.chatbot.models.VerificationToken;
import com.g4.chatbot.repos.AdminRepository;
import com.g4.chatbot.repos.PasswordResetTokenRepository;
import com.g4.chatbot.repos.UserRepository;
import com.g4.chatbot.repos.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminTokenManagementService {
    
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    
    /**
     * Get all password reset tokens with pagination and filtering
     */
    @Transactional(readOnly = true)
    public TokenListResponse<PasswordResetTokenDTO> getAllPasswordResetTokens(
            String userType,
            Boolean includeExpired,
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {
        Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") ? 
                Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<PasswordResetToken> tokenPage;
        
        if (userType != null && !userType.isEmpty()) {
            tokenPage = passwordResetTokenRepository.findByUserTypeOrderByCreatedDateDesc(userType, pageable);
        } else if (Boolean.FALSE.equals(includeExpired)) {
            tokenPage = passwordResetTokenRepository.findByExpiryDateBeforeOrderByCreatedDateDesc(
                    LocalDateTime.now(), pageable);
        } else {
            tokenPage = passwordResetTokenRepository.findAllByOrderByCreatedDateDesc(pageable);
        }
        
        List<PasswordResetTokenDTO> tokenDTOs = tokenPage.getContent().stream()
                .map(this::convertPasswordResetTokenToDTO)
                .collect(Collectors.toList());
        
        return TokenListResponse.<PasswordResetTokenDTO>builder()
                .tokens(tokenDTOs)
                .currentPage(tokenPage.getNumber())
                .totalPages(tokenPage.getTotalPages())
                .totalElements(tokenPage.getTotalElements())
                .pageSize(tokenPage.getSize())
                .hasNext(tokenPage.hasNext())
                .hasPrevious(tokenPage.hasPrevious())
                .build();
    }
    
    /**
     * Get password reset token by ID
     */
    @Transactional(readOnly = true)
    public PasswordResetTokenDTO getPasswordResetTokenById(Long tokenId) {
        PasswordResetToken token = passwordResetTokenRepository.findById(tokenId)
                .orElseThrow(() -> new RuntimeException("Password reset token not found with ID: " + tokenId));
        
        return convertPasswordResetTokenToDTO(token);
    }
    
    /**
     * Get all verification tokens with pagination and filtering
     */
    @Transactional(readOnly = true)
    public TokenListResponse<VerificationTokenDTO> getAllVerificationTokens(
            String userType,
            Boolean includeExpired,
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {
        Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") ? 
                Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<VerificationToken> tokenPage;
        
        if (userType != null && !userType.isEmpty()) {
            tokenPage = verificationTokenRepository.findByUserTypeOrderByCreatedDateDesc(userType, pageable);
        } else if (Boolean.FALSE.equals(includeExpired)) {
            tokenPage = verificationTokenRepository.findByExpiryDateBeforeOrderByCreatedDateDesc(
                    LocalDateTime.now(), pageable);
        } else {
            tokenPage = verificationTokenRepository.findAllByOrderByCreatedDateDesc(pageable);
        }
        
        List<VerificationTokenDTO> tokenDTOs = tokenPage.getContent().stream()
                .map(this::convertVerificationTokenToDTO)
                .collect(Collectors.toList());
        
        return TokenListResponse.<VerificationTokenDTO>builder()
                .tokens(tokenDTOs)
                .currentPage(tokenPage.getNumber())
                .totalPages(tokenPage.getTotalPages())
                .totalElements(tokenPage.getTotalElements())
                .pageSize(tokenPage.getSize())
                .hasNext(tokenPage.hasNext())
                .hasPrevious(tokenPage.hasPrevious())
                .build();
    }
    
    /**
     * Get verification token by ID
     */
    @Transactional(readOnly = true)
    public VerificationTokenDTO getVerificationTokenById(Long tokenId) {
        VerificationToken token = verificationTokenRepository.findById(tokenId)
                .orElseThrow(() -> new RuntimeException("Verification token not found with ID: " + tokenId));
        
        return convertVerificationTokenToDTO(token);
    }
    
    /**
     * Delete password reset token
     */
    @Transactional
    public void deletePasswordResetToken(Long tokenId) {
        if (!passwordResetTokenRepository.existsById(tokenId)) {
            throw new RuntimeException("Password reset token not found with ID: " + tokenId);
        }
        
        passwordResetTokenRepository.deleteById(tokenId);
        log.info("Deleted password reset token with ID: {}", tokenId);
    }
    
    /**
     * Delete verification token
     */
    @Transactional
    public void deleteVerificationToken(Long tokenId) {
        if (!verificationTokenRepository.existsById(tokenId)) {
            throw new RuntimeException("Verification token not found with ID: " + tokenId);
        }
        
        verificationTokenRepository.deleteById(tokenId);
        log.info("Deleted verification token with ID: {}", tokenId);
    }
    
    /**
     * Delete expired tokens (cleanup utility)
     */
    @Transactional
    public int deleteExpiredPasswordResetTokens() {
        Page<PasswordResetToken> expiredTokens = passwordResetTokenRepository
                .findByExpiryDateBeforeOrderByCreatedDateDesc(LocalDateTime.now(), 
                        PageRequest.of(0, 1000));
        
        int count = expiredTokens.getContent().size();
        passwordResetTokenRepository.deleteAll(expiredTokens.getContent());
        
        log.info("Deleted {} expired password reset tokens", count);
        return count;
    }
    
    /**
     * Delete expired verification tokens (cleanup utility)
     */
    @Transactional
    public int deleteExpiredVerificationTokens() {
        Page<VerificationToken> expiredTokens = verificationTokenRepository
                .findByExpiryDateBeforeOrderByCreatedDateDesc(LocalDateTime.now(), 
                        PageRequest.of(0, 1000));
        
        int count = expiredTokens.getContent().size();
        verificationTokenRepository.deleteAll(expiredTokens.getContent());
        
        log.info("Deleted {} expired verification tokens", count);
        return count;
    }
    
    /**
     * Convert PasswordResetToken to DTO
     */
    private PasswordResetTokenDTO convertPasswordResetTokenToDTO(PasswordResetToken token) {
        String username = "Unknown";
        String email = "Unknown";
        
        if ("user".equals(token.getUserType())) {
            User user = userRepository.findById(token.getUserId()).orElse(null);
            if (user != null) {
                username = user.getUsername();
                email = user.getEmail();
            }
        } else if ("admin".equals(token.getUserType())) {
            Admin admin = adminRepository.findById(token.getUserId()).orElse(null);
            if (admin != null) {
                username = admin.getUsername();
                email = admin.getEmail();
            }
        }
        
        return PasswordResetTokenDTO.builder()
                .id(token.getId())
                .token(token.getToken())
                .userId(token.getUserId())
                .userType(token.getUserType())
                .username(username)
                .email(email)
                .expiryDate(token.getExpiryDate())
                .createdDate(token.getCreatedDate())
                .attemptCount(token.getAttemptCount())
                .requestingIp(token.getRequestingIp())
                .expired(token.isExpired())
                .build();
    }
    
    /**
     * Convert VerificationToken to DTO
     */
    private VerificationTokenDTO convertVerificationTokenToDTO(VerificationToken token) {
        String username = "Unknown";
        String email = "Unknown";
        
        if ("user".equals(token.getUserType())) {
            User user = userRepository.findById(token.getUserId()).orElse(null);
            if (user != null) {
                username = user.getUsername();
                email = user.getEmail();
            }
        } else if ("admin".equals(token.getUserType())) {
            Admin admin = adminRepository.findById(token.getUserId()).orElse(null);
            if (admin != null) {
                username = admin.getUsername();
                email = admin.getEmail();
            }
        }
        
        return VerificationTokenDTO.builder()
                .id(token.getId())
                .token(token.getToken())
                .userId(token.getUserId())
                .userType(token.getUserType())
                .username(username)
                .email(email)
                .expiryDate(token.getExpiryDate())
                .createdDate(token.getCreatedDate())
                .expired(token.isExpired())
                .build();
    }
}
