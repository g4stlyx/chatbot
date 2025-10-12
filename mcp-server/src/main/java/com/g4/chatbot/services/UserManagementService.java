package com.g4.chatbot.services;

import com.g4.chatbot.dto.admin.*;
import com.g4.chatbot.exception.BadRequestException;
import com.g4.chatbot.exception.ResourceNotFoundException;
import com.g4.chatbot.models.User;
import com.g4.chatbot.repos.AdminRepository;
import com.g4.chatbot.repos.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    
    @Autowired
    private AdminActivityLogger activityLogger;
    
    /**
     * Get all users with pagination
     */
    public UserListResponse getAllUsers(int page, int size, String sortBy, String sortDirection, Long adminId, HttpServletRequest httpRequest) {
        log.info("Admin {} fetching users - page: {}, size: {}, sortBy: {}, direction: {}", 
            adminId, page, size, sortBy, sortDirection);
        
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? 
            Sort.Direction.DESC : Sort.Direction.ASC;
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<User> userPage = userRepository.findAll(pageable);
        
        List<UserManagementDTO> users = userPage.getContent().stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
        
        // Log the READ activity
        Map<String, Object> details = new HashMap<>();
        details.put("page", page);
        details.put("size", size);
        details.put("sortBy", sortBy);
        details.put("sortDirection", sortDirection);
        details.put("resultCount", users.size());
        details.put("totalItems", userPage.getTotalElements());
        activityLogger.logActivity(adminId, "READ", "User", "list", details, httpRequest);
        
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
    public UserManagementDTO getUserById(Long userId, Long adminId, HttpServletRequest httpRequest) {
        log.info("Admin {} fetching user by ID: {}", adminId, userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        // Log the READ activity
        Map<String, Object> details = new HashMap<>();
        details.put("username", user.getUsername());
        details.put("email", user.getEmail());
        details.put("isActive", user.getIsActive());
        activityLogger.logActivity(adminId, "READ", "User", userId.toString(), details, httpRequest);
        
        return mapToDTO(user);
    }
    
    /**
     * Create a new user (by admin)
     */
    @Transactional
    public UserManagementDTO createUser(Long adminId, CreateUserRequest request, HttpServletRequest httpRequest) {
        log.info("Admin {} creating new user: {}", adminId, request.getUsername());
        
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
        
        // Log activity
        Map<String, Object> details = new HashMap<>();
        details.put("username", user.getUsername());
        details.put("email", user.getEmail());
        details.put("firstName", user.getFirstName());
        details.put("lastName", user.getLastName());
        details.put("emailVerified", user.getEmailVerified());
        activityLogger.logCreate(adminId, "user", user.getId().toString(), details, httpRequest);
        
        return mapToDTO(user);
    }
    
    /**
     * Update user by ID
     */
    @Transactional
    public UserManagementDTO updateUser(Long adminId, Long userId, UpdateUserRequest request, HttpServletRequest httpRequest) {
        log.info("Admin {} updating user ID: {}", adminId, userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        // Track changes for logging
        Map<String, Object> changes = new HashMap<>();
        
        // Check if email is being changed and if it's already taken
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail()) || 
                adminRepository.existsByEmail(request.getEmail())) {
                throw new BadRequestException("Email is already in use");
            }
            changes.put("email", Map.of("old", user.getEmail(), "new", request.getEmail()));
            user.setEmail(request.getEmail());
        }
        
        // Update fields if provided
        if (request.getFirstName() != null && !request.getFirstName().equals(user.getFirstName())) {
            changes.put("firstName", Map.of("old", user.getFirstName(), "new", request.getFirstName()));
            user.setFirstName(request.getFirstName());
        }
        
        if (request.getLastName() != null && !request.getLastName().equals(user.getLastName())) {
            changes.put("lastName", Map.of("old", user.getLastName(), "new", request.getLastName()));
            user.setLastName(request.getLastName());
        }
        
        if (request.getProfilePicture() != null) {
            user.setProfilePicture(request.getProfilePicture());
        }
        
        if (request.getIsActive() != null && !request.getIsActive().equals(user.getIsActive())) {
            changes.put("isActive", Map.of("old", user.getIsActive(), "new", request.getIsActive()));
            user.setIsActive(request.getIsActive());
        }
        
        if (request.getEmailVerified() != null && !request.getEmailVerified().equals(user.getEmailVerified())) {
            changes.put("emailVerified", Map.of("old", user.getEmailVerified(), "new", request.getEmailVerified()));
            user.setEmailVerified(request.getEmailVerified());
        }
        
        user = userRepository.save(user);
        log.info("User updated successfully: {}", userId);
        
        // Log activity
        if (!changes.isEmpty()) {
            activityLogger.logUpdate(adminId, "user", userId.toString(), changes, httpRequest);
        }
        
        return mapToDTO(user);
    }
    
    /**
     * Delete user by ID (soft delete - deactivate)
     */
    @Transactional
    public void deleteUser(Long adminId, Long userId, HttpServletRequest httpRequest) {
        log.info("Admin {} deleting (deactivating) user ID: {}", adminId, userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        user.setIsActive(false);
        userRepository.save(user);
        
        log.info("User deactivated successfully: {}", userId);
        
        // Log activity
        activityLogger.logDelete(adminId, "user", userId.toString(), httpRequest);
    }
    
    /**
     * Activate user
     */
    @Transactional
    public UserManagementDTO activateUser(Long adminId, Long userId, HttpServletRequest httpRequest) {
        log.info("Admin {} activating user ID: {}", adminId, userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        user.setIsActive(true);
        user.setLoginAttempts(0);
        user.setLockedUntil(null);
        
        user = userRepository.save(user);
        log.info("User activated successfully: {}", userId);
        
        // Log activity
        activityLogger.logActivate(adminId, "user", userId.toString(), httpRequest);
        
        return mapToDTO(user);
    }
    
    /**
     * Deactivate user
     */
    @Transactional
    public UserManagementDTO deactivateUser(Long adminId, Long userId, HttpServletRequest httpRequest) {
        log.info("Admin {} deactivating user ID: {}", adminId, userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        user.setIsActive(false);
        user = userRepository.save(user);
        
        log.info("User deactivated successfully: {}", userId);
        
        // Log activity
        activityLogger.logDeactivate(adminId, "user", userId.toString(), httpRequest);
        
        return mapToDTO(user);
    }
    
    /**
     * Reset user password (by admin)
     */
    @Transactional
    public void resetUserPassword(Long adminId, Long userId, ResetUserPasswordRequest request, HttpServletRequest httpRequest) {
        log.info("Admin {} resetting password for user ID: {}", adminId, userId);
        
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
        
        // Log activity
        Map<String, Object> details = new HashMap<>();
        details.put("passwordReset", true);
        details.put("accountUnlocked", true);
        activityLogger.logActivity(adminId, "RESET_PASSWORD", "user", userId.toString(), details, httpRequest);
    }
    
    /**
     * Unlock user account
     */
    @Transactional
    public UserManagementDTO unlockUser(Long adminId, Long userId, HttpServletRequest httpRequest) {
        log.info("Admin {} unlocking user account ID: {}", adminId, userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        user.setLoginAttempts(0);
        user.setLockedUntil(null);
        
        user = userRepository.save(user);
        log.info("User account unlocked successfully: {}", userId);
        
        // Log activity
        Map<String, Object> details = new HashMap<>();
        details.put("action", "unlocked");
        activityLogger.logActivity(adminId, "UNLOCK", "user", userId.toString(), details, httpRequest);
        
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
