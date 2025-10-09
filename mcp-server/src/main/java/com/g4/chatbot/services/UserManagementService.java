package com.g4.chatbot.services;

import com.g4.chatbot.dto.admin.*;
import com.g4.chatbot.exception.BadRequestException;
import com.g4.chatbot.exception.ResourceNotFoundException;
import com.g4.chatbot.models.User;
import com.g4.chatbot.repos.AdminRepository;
import com.g4.chatbot.repos.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserManagementService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AdminRepository adminRepository;
    
    @Autowired
    private PasswordService passwordService;
    
    /**
     * Get all users with pagination
     */
    public UserListResponse getAllUsers(int page, int size, String sortBy, String sortDirection) {
        log.info("Fetching users - page: {}, size: {}, sortBy: {}, direction: {}", 
            page, size, sortBy, sortDirection);
        
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? 
            Sort.Direction.DESC : Sort.Direction.ASC;
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<User> userPage = userRepository.findAll(pageable);
        
        List<UserManagementDTO> users = userPage.getContent().stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
        
        return UserListResponse.builder()
            .users(users)
            .currentPage(userPage.getNumber())
            .totalPages(userPage.getTotalPages())
            .totalItems(userPage.getTotalElements())
            .pageSize(userPage.getSize())
            .build();
    }
    
    /**
     * Get user by ID
     */
    public UserManagementDTO getUserById(Long userId) {
        log.info("Fetching user by ID: {}", userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        return mapToDTO(user);
    }
    
    /**
     * Create a new user (by admin)
     */
    @Transactional
    public UserManagementDTO createUser(CreateUserRequest request) {
        log.info("Admin creating new user: {}", request.getUsername());
        
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername()) || 
            adminRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username already exists");
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail()) || 
            adminRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }
        
        // Create new user
        String salt = passwordService.generateSalt();
        String hashedPassword = passwordService.hashPassword(request.getPassword(), salt);
        
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(hashedPassword);
        user.setSalt(salt);
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setProfilePicture(request.getProfilePicture());
        user.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        user.setEmailVerified(request.getEmailVerified() != null ? request.getEmailVerified() : false);
        
        user = userRepository.save(user);
        log.info("User created successfully with ID: {}", user.getId());
        
        return mapToDTO(user);
    }
    
    /**
     * Update user by ID
     */
    @Transactional
    public UserManagementDTO updateUser(Long userId, UpdateUserRequest request) {
        log.info("Updating user ID: {}", userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        // Check if email is being changed and if it's already taken
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail()) || 
                adminRepository.existsByEmail(request.getEmail())) {
                throw new BadRequestException("Email is already in use");
            }
            user.setEmail(request.getEmail());
        }
        
        // Update fields if provided
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        
        if (request.getProfilePicture() != null) {
            user.setProfilePicture(request.getProfilePicture());
        }
        
        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
        }
        
        if (request.getEmailVerified() != null) {
            user.setEmailVerified(request.getEmailVerified());
        }
        
        user = userRepository.save(user);
        log.info("User updated successfully: {}", userId);
        
        return mapToDTO(user);
    }
    
    /**
     * Delete user by ID (soft delete - deactivate)
     */
    @Transactional
    public void deleteUser(Long userId) {
        log.info("Deleting (deactivating) user ID: {}", userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        user.setIsActive(false);
        userRepository.save(user);
        
        log.info("User deactivated successfully: {}", userId);
    }
    
    /**
     * Activate user
     */
    @Transactional
    public UserManagementDTO activateUser(Long userId) {
        log.info("Activating user ID: {}", userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        user.setIsActive(true);
        user.setLoginAttempts(0);
        user.setLockedUntil(null);
        
        user = userRepository.save(user);
        log.info("User activated successfully: {}", userId);
        
        return mapToDTO(user);
    }
    
    /**
     * Deactivate user
     */
    @Transactional
    public UserManagementDTO deactivateUser(Long userId) {
        log.info("Deactivating user ID: {}", userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        user.setIsActive(false);
        user = userRepository.save(user);
        
        log.info("User deactivated successfully: {}", userId);
        
        return mapToDTO(user);
    }
    
    /**
     * Reset user password (by admin)
     */
    @Transactional
    public void resetUserPassword(Long userId, ResetUserPasswordRequest request) {
        log.info("Admin resetting password for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        // Generate new salt and hash new password
        String newSalt = passwordService.generateSalt();
        String newHashedPassword = passwordService.hashPassword(request.getNewPassword(), newSalt);
        
        user.setSalt(newSalt);
        user.setPasswordHash(newHashedPassword);
        user.setLoginAttempts(0);
        user.setLockedUntil(null);
        
        userRepository.save(user);
        log.info("Password reset successfully for user ID: {}", userId);
    }
    
    /**
     * Unlock user account
     */
    @Transactional
    public UserManagementDTO unlockUser(Long userId) {
        log.info("Unlocking user account ID: {}", userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        user.setLoginAttempts(0);
        user.setLockedUntil(null);
        
        user = userRepository.save(user);
        log.info("User account unlocked successfully: {}", userId);
        
        return mapToDTO(user);
    }
    
    /**
     * Search users by username or email
     */
    public UserListResponse searchUsers(String searchTerm, int page, int size) {
        log.info("Searching users with term: {}", searchTerm);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "username"));
        Page<User> userPage = userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            searchTerm, searchTerm, pageable);
        
        List<UserManagementDTO> users = userPage.getContent().stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
        
        return UserListResponse.builder()
            .users(users)
            .currentPage(userPage.getNumber())
            .totalPages(userPage.getTotalPages())
            .totalItems(userPage.getTotalElements())
            .pageSize(userPage.getSize())
            .build();
    }
    
    /**
     * Map User entity to UserManagementDTO
     */
    private UserManagementDTO mapToDTO(User user) {
        return UserManagementDTO.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .profilePicture(user.getProfilePicture())
            .isActive(user.getIsActive())
            .emailVerified(user.getEmailVerified())
            .loginAttempts(user.getLoginAttempts())
            .lockedUntil(user.getLockedUntil())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .lastLoginAt(user.getLastLoginAt())
            .build();
    }
}
