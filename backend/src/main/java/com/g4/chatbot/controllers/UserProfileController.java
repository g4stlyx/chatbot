package com.g4.chatbot.controllers;

import com.g4.chatbot.dto.profile.ChangePasswordRequest;
import com.g4.chatbot.dto.profile.UpdateUserProfileRequest;
import com.g4.chatbot.dto.profile.UserProfileDTO;
import com.g4.chatbot.services.UserProfileService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/user/profile")
@Slf4j
public class UserProfileController {
    
    @Autowired
    private UserProfileService userProfileService;
    
    /**
     * Get current user's profile
     * GET /api/v1/user/profile
     */
    @GetMapping
    public ResponseEntity<UserProfileDTO> getUserProfile(Authentication authentication) {
        Long userId = (Long) authentication.getDetails();
        log.info("User {} fetching their profile", userId);
        
        UserProfileDTO profile = userProfileService.getUserProfile(userId);
        return ResponseEntity.ok(profile);
    }
    
    /**
     * Update current user's profile
     * PUT /api/v1/user/profile
     */
    @PutMapping
    public ResponseEntity<UserProfileDTO> updateUserProfile(
            @Valid @RequestBody UpdateUserProfileRequest request,
            Authentication authentication) {
        
        Long userId = (Long) authentication.getDetails();
        log.info("User {} updating their profile", userId);
        
        UserProfileDTO profile = userProfileService.updateUserProfile(userId, request);
        return ResponseEntity.ok(profile);
    }
    
    /**
     * Change current user's password
     * POST /api/v1/user/profile/change-password
     */
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        
        Long userId = (Long) authentication.getDetails();
        log.info("User {} changing their password", userId);
        
        userProfileService.changePassword(userId, request);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Password changed successfully");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Deactivate current user's account
     * POST /api/v1/user/profile/deactivate
     */
    @PostMapping("/deactivate")
    public ResponseEntity<Map<String, Object>> deactivateAccount(Authentication authentication) {
        Long userId = (Long) authentication.getDetails();
        log.info("User {} deactivating their account", userId);
        
        userProfileService.deactivateAccount(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Account deactivated successfully");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Reactivate current user's account
     * POST /api/v1/user/profile/reactivate
     */
    @PostMapping("/reactivate")
    public ResponseEntity<Map<String, Object>> reactivateAccount(Authentication authentication) {
        Long userId = (Long) authentication.getDetails();
        log.info("User {} reactivating their account", userId);
        
        userProfileService.reactivateAccount(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Account reactivated successfully");
        
        return ResponseEntity.ok(response);
    }
}
