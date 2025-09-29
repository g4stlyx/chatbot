package com.g4.chatbot.controllers;

import com.g4.chatbot.dto.AuthResponse;
import com.g4.chatbot.dto.LoginRequest;
import com.g4.chatbot.dto.RegisterRequest;
import com.g4.chatbot.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private AuthService authService;
    
    @Autowired
    private InputValidationService inputValidationService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request, 
                                               HttpServletRequest httpRequest) {
        // Additional validation on the username
        request.setUsername(inputValidationService.validateAndSanitizeUsername(request.getUsername()));
        
        // Additional validation on the email
        if (request.getEmail() != null) {
            request.setEmail(inputValidationService.validateAndSanitizeEmail(request.getEmail()));
        }
        
        // Additional validation on the phone number
        if (request.getPhoneNumber() != null) {
            request.setPhoneNumber(inputValidationService.validateAndSanitizePhone(request.getPhoneNumber()));
        }
        
        // Validate name
        if (request.getName() != null) {
            request.setName(inputValidationService.validateAndSanitizeText(request.getName(), "Name", 100, true));
        }
        
        // Password is validated but not sanitized
        inputValidationService.validatePassword(request.getPassword());
        
        return ResponseEntity.ok(authService.register(request, httpRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, 
                                            HttpServletRequest httpRequest) {
        // Validate and sanitize username
        request.setUsername(inputValidationService.validateAndSanitizeUsername(request.getUsername()));
        
        // Password is validated but not sanitized
        inputValidationService.validatePassword(request.getPassword());
        
        return ResponseEntity.ok(authService.login(request, httpRequest));
    }
    
    /**
     * Endpoint to verify if a password is valid for a given username and role.
     * This is primarily used before allowing a user to change their password.
     */
    @PostMapping("/verify-password")
    public ResponseEntity<Map<String, Object>> verifyPassword(
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {
        
        // Extract values
        String username = request.get("username");
        String role = request.get("role");
        String password = request.get("password");
        
        // Validate required fields
        if (username == null || role == null || password == null) {
            throw new InputValidationException("Username, role, and password are required");
        }
        
        // Validate and sanitize username
        username = inputValidationService.validateAndSanitizeUsername(username);
        
        // Validate and sanitize role
        role = inputValidationService.validateAndSanitizeText(role, "Role", 50, true);
        
        // Validate password (no sanitization)
        inputValidationService.validatePassword(password);
        
        boolean isValid = authService.verifyPassword(username, role, password, httpRequest);
        
        Map<String, Object> response = new HashMap<>();
        response.put("valid", isValid);
        response.put("message", isValid ? "Password is valid" : "Password is invalid");
        
        return ResponseEntity.ok(response);
    }
}