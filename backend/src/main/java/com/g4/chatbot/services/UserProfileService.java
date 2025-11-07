package com.g4.chatbot.services;

import com.g4.chatbot.dto.profile.ChangePasswordRequest;
import com.g4.chatbot.dto.profile.UpdateUserProfileRequest;
import com.g4.chatbot.dto.profile.UserProfileDTO;
import com.g4.chatbot.exception.ResourceNotFoundException;
import com.g4.chatbot.exception.BadRequestException;
import com.g4.chatbot.models.User;
import com.g4.chatbot.repos.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class UserProfileService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordService passwordService;
    
    /**
     * Get user profile by user ID
     */
    public UserProfileDTO getUserProfile(Long userId) {
        log.info("Fetching profile for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        return mapToDTO(user);
    }
    
    /**
     * Update user profile
     */
    @Transactional
    public UserProfileDTO updateUserProfile(Long userId, UpdateUserProfileRequest request) {
        log.info("Updating profile for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        // Check if email is being changed and if it's already taken
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BadRequestException("Email is already in use");
            }
            user.setEmail(request.getEmail());
            // When email is changed, set email verification to false
            user.setEmailVerified(false);
            log.info("Email changed for user ID: {}. Email verification reset.", userId);
        }
        
        // Update other fields if provided
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        
        if (request.getProfilePicture() != null) {
            user.setProfilePicture(request.getProfilePicture());
        }
        
        user = userRepository.save(user);
        log.info("Profile updated successfully for user ID: {}", userId);
        
        return mapToDTO(user);
    }
    
    /**
     * Change user password
     */
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        log.info("Changing password for user ID: {}", userId);
        
        // Validate new password and confirm password match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("New password and confirm password do not match");
        }
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        // Verify current password
        boolean isValidPassword = passwordService.verifyPassword(
            request.getCurrentPassword(), user.getSalt(), user.getPasswordHash());
        
        if (!isValidPassword) {
            throw new BadRequestException("Current password is incorrect");
        }
        
        // Check if new password is same as current password
        if (request.getCurrentPassword().equals(request.getNewPassword())) {
            throw new BadRequestException("New password must be different from current password");
        }
        
        // Generate new salt and hash new password
        String newSalt = passwordService.generateSalt();
        String newHashedPassword = passwordService.hashPassword(request.getNewPassword(), newSalt);
        
        user.setSalt(newSalt);
        user.setPasswordHash(newHashedPassword);
        
        userRepository.save(user);
        log.info("Password changed successfully for user ID: {}", userId);
    }
    
    /**
     * Deactivate user account
     */
    @Transactional
    public void deactivateAccount(Long userId) {
        log.info("Deactivating account for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        user.setIsActive(false);
        userRepository.save(user);
        
        log.info("Account deactivated successfully for user ID: {}", userId);
    }
    
    /**
     * Reactivate user account
     */
    @Transactional
    public void reactivateAccount(Long userId) {
        log.info("Reactivating account for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        user.setIsActive(true);
        userRepository.save(user);
        
        log.info("Account reactivated successfully for user ID: {}", userId);
    }
    
    /**
     * Map User entity to UserProfileDTO
     */
    private UserProfileDTO mapToDTO(User user) {
        return UserProfileDTO.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .profilePicture(user.getProfilePicture())
            .isActive(user.getIsActive())
            .emailVerified(user.getEmailVerified())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .lastLoginAt(user.getLastLoginAt())
            .build();
    }
}
