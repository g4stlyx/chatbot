package com.g4.chatbot.services;

import com.g4.chatbot.dto.auth.*;
import com.g4.chatbot.models.Admin;
import com.g4.chatbot.models.PasswordResetToken;
import com.g4.chatbot.models.User;
import com.g4.chatbot.models.VerificationToken;
import com.g4.chatbot.repos.AdminRepository;
import com.g4.chatbot.repos.PasswordResetTokenRepository;
import com.g4.chatbot.repos.UserRepository;
import com.g4.chatbot.repos.VerificationTokenRepository;
import com.g4.chatbot.security.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AdminRepository adminRepository;
    
    @Autowired
    private PasswordService passwordService;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private VerificationTokenRepository verificationTokenRepository;
    
    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;
    
    /**
     * Register a new user (only users can register via API, admins must be created manually)
     */
    @Transactional
    public AuthResponse register(RegisterRequest request, HttpServletRequest httpRequest) {
        try {
            // Check if username already exists in both tables
            if (userRepository.existsByUsername(request.getUsername()) || 
                adminRepository.existsByUsername(request.getUsername())) {
                return AuthResponse.builder()
                    .success(false)
                    .message("Username already exists")
                    .build();
            }
            
            // Check if email already exists in both tables
            if (userRepository.existsByEmail(request.getEmail()) || 
                adminRepository.existsByEmail(request.getEmail())) {
                return AuthResponse.builder()
                    .success(false)
                    .message("Email already exists")
                    .build();
            }
            
            // Only register regular users via API
            return registerUser(request, httpRequest);
            
        } catch (Exception e) {
            log.error("Registration failed for user: {}", request.getUsername(), e);
            return AuthResponse.builder()
                .success(false)
                .message("Registration failed. Please try again.")
                .build();
        }
    }
    
    private AuthResponse registerUser(RegisterRequest request, HttpServletRequest httpRequest) {
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
        user.setIsActive(true);
        user.setEmailVerified(false);
        
        user = userRepository.save(user);
        
        // Create verification token
        VerificationToken verificationToken = new VerificationToken(user.getId(), "user");
        verificationTokenRepository.save(verificationToken);
        
        // Send verification email
        emailService.sendVerificationEmail(user.getEmail(), verificationToken.getToken(), 
            user.getFirstName() != null ? user.getFirstName() : user.getUsername());
        
        // Generate tokens for automatic login after registration
        String accessToken = jwtUtils.generateToken(user.getUsername(), user.getId(), "user", null);
        String refreshToken = jwtUtils.generateRefreshToken(user.getUsername());
        
        return AuthResponse.builder()
            .success(true)
            .message("User registered successfully. Please check your email to verify your account.")
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .expiresIn(jwtUtils.getAccessTokenExpiration())
            .user(AuthResponse.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .profilePicture(user.getProfilePicture())
                .isActive(user.getIsActive())
                .emailVerified(user.getEmailVerified())
                .userType("user")
                .lastLoginAt(user.getLastLoginAt())
                .build())
            .build();
    }
    
    
    /**
     * Authenticate user (supports both username and email login)
     */
    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        try {
            String userType = request.getUserType() != null ? request.getUserType().toLowerCase() : null;
            
            // If userType is specified, only search in that specific type
            if ("admin".equals(userType)) {
                // Only search in admin table
                Optional<Admin> adminOpt = adminRepository.findByUsernameOrEmail(
                    request.getUsername(), request.getUsername());
                
                if (adminOpt.isPresent()) {
                    return authenticateAdmin(adminOpt.get(), request.getPassword(), httpRequest);
                } else {
                    return AuthResponse.builder()
                        .success(false)
                        .message("Invalid admin credentials")
                        .build();
                }
            } else if ("user".equals(userType)) {
                // Only search in user table
                Optional<User> userOpt = userRepository.findByUsernameOrEmail(
                    request.getUsername(), request.getUsername());
                
                if (userOpt.isPresent()) {
                    return authenticateUser(userOpt.get(), request.getPassword(), httpRequest);
                } else {
                    return AuthResponse.builder()
                        .success(false)
                        .message("Invalid user credentials")
                        .build();
                }
            } else {
                // Legacy mode: try both tables (user first, then admin)
                Optional<User> userOpt = userRepository.findByUsernameOrEmail(
                    request.getUsername(), request.getUsername());
                    
                if (userOpt.isPresent()) {
                    return authenticateUser(userOpt.get(), request.getPassword(), httpRequest);
                }
                
                Optional<Admin> adminOpt = adminRepository.findByUsernameOrEmail(
                    request.getUsername(), request.getUsername());
                    
                if (adminOpt.isPresent()) {
                    return authenticateAdmin(adminOpt.get(), request.getPassword(), httpRequest);
                }
                
                return AuthResponse.builder()
                    .success(false)
                    .message("Invalid credentials")
                    .build();
            }
            
        } catch (Exception e) {
            log.error("Login failed for user: {}", request.getUsername(), e);
            return AuthResponse.builder()
                .success(false)
                .message("Login failed. Please try again.")
                .build();
        }
    }
    
    private AuthResponse authenticateUser(User user, String password, HttpServletRequest httpRequest) {
        // Check if account is active
        if (!user.getIsActive()) {
            return AuthResponse.builder()
                .success(false)
                .message("Account is deactivated")
                .build();
        }
        
        // Check if email is verified
        if (!user.getEmailVerified()) {
            return AuthResponse.builder()
                .success(false)
                .message("Email must be verified before login. Please check your email for verification link.")
                .build();
        }
        
        // Check if account is locked
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            return AuthResponse.builder()
                .success(false)
                .message("Account is temporarily locked. Please try again later.")
                .build();
        }
        
        // Verify password
        boolean isValidPassword = passwordService.verifyPassword(password, user.getSalt(), user.getPasswordHash());
        
        if (!isValidPassword) {
            // Increment login attempts
            user.setLoginAttempts(user.getLoginAttempts() + 1);
            
            // Lock account after 5 failed attempts
            if (user.getLoginAttempts() >= 5) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(15));
            }
            
            userRepository.save(user);
            
            return AuthResponse.builder()
                .success(false)
                .message("Invalid credentials")
                .build();
        }
        
        // Reset login attempts on successful login
        user.setLoginAttempts(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        
        // Generate tokens
        String accessToken = jwtUtils.generateToken(user.getUsername(), user.getId(), "user", null);
        String refreshToken = jwtUtils.generateRefreshToken(user.getUsername());
        
        return AuthResponse.builder()
            .success(true)
            .message("Login successful")
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .expiresIn(jwtUtils.getAccessTokenExpiration())
            .user(AuthResponse.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .profilePicture(user.getProfilePicture())
                .isActive(user.getIsActive())
                .emailVerified(user.getEmailVerified())
                .userType("user")
                .lastLoginAt(user.getLastLoginAt())
                .build())
            .build();
    }
    
    private AuthResponse authenticateAdmin(Admin admin, String password, HttpServletRequest httpRequest) {
        // Check if account is active
        if (!admin.getIsActive()) {
            return AuthResponse.builder()
                .success(false)
                .message("Admin account is deactivated")
                .build();
        }
        
        // Check if account is locked
        if (admin.getLockedUntil() != null && admin.getLockedUntil().isAfter(LocalDateTime.now())) {
            return AuthResponse.builder()
                .success(false)
                .message("Admin account is temporarily locked. Please try again later.")
                .build();
        }
        
        // Verify password
        boolean isValidPassword = passwordService.verifyPassword(password, admin.getSalt(), admin.getPasswordHash());
        
        if (!isValidPassword) {
            // Increment login attempts
            admin.setLoginAttempts(admin.getLoginAttempts() + 1);
            
            // Lock account after 5 failed attempts
            if (admin.getLoginAttempts() >= 5) {
                admin.setLockedUntil(LocalDateTime.now().plusMinutes(15));
            }
            
            adminRepository.save(admin);
            
            return AuthResponse.builder()
                .success(false)
                .message("Invalid credentials")
                .build();
        }
        
        // Reset login attempts on successful login
        admin.setLoginAttempts(0);
        admin.setLockedUntil(null);
        admin.setLastLoginAt(LocalDateTime.now());
        adminRepository.save(admin);
        
        // Generate tokens with admin level
        String accessToken = jwtUtils.generateToken(admin.getUsername(), admin.getId(), "admin", admin.getLevel());
        String refreshToken = jwtUtils.generateRefreshToken(admin.getUsername());
        
        return AuthResponse.builder()
            .success(true)
            .message("Admin login successful")
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .expiresIn(jwtUtils.getAccessTokenExpiration())
            .user(AuthResponse.UserInfo.builder()
                .id(admin.getId())
                .username(admin.getUsername())
                .email(admin.getEmail())
                .firstName(admin.getFirstName())
                .lastName(admin.getLastName())
                .profilePicture(admin.getProfilePicture())
                .isActive(admin.getIsActive())
                .emailVerified(true) // Admins are auto-verified
                .userType("admin")
                .level(admin.getLevel())
                .lastLoginAt(admin.getLastLoginAt())
                .build())
            .build();
    }
    
    /**
     * Verify user password (for password change operations)
     */
    public boolean verifyPassword(VerifyPasswordRequest request, HttpServletRequest httpRequest) {
        try {
            // Try to find user first
            Optional<User> userOpt = userRepository.findByUsernameOrEmail(
                request.getUsername(), request.getUsername());
            
            if (userOpt.isPresent()) {
                return passwordService.verifyPassword(request.getPassword(), 
                    userOpt.get().getSalt(), userOpt.get().getPasswordHash());
            }
            
            // Try to find admin
            Optional<Admin> adminOpt = adminRepository.findByUsernameOrEmail(
                request.getUsername(), request.getUsername());
            
            if (adminOpt.isPresent()) {
                return passwordService.verifyPassword(request.getPassword(), 
                    adminOpt.get().getSalt(), adminOpt.get().getPasswordHash());
            }
            
            return false;
            
        } catch (Exception e) {
            log.error("Password verification failed for user: {}", request.getUsername(), e);
            return false;
        }
    }
    
    /**
     * Initiate forgot password process
     */
    @Transactional
    public boolean forgotPassword(ForgotPasswordRequest request, HttpServletRequest httpRequest) {
        try {
            String clientIp = getClientIpAddress(httpRequest);
            
            // Try to find user by email
            Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
            if (userOpt.isPresent()) {
                createPasswordResetToken(userOpt.get().getId(), "user", request.getEmail(), clientIp);
                return true;
            }
            
            // Try to find admin by email
            Optional<Admin> adminOpt = adminRepository.findByEmail(request.getEmail());
            if (adminOpt.isPresent()) {
                createPasswordResetToken(adminOpt.get().getId(), "admin", request.getEmail(), clientIp);
                return true;
            }
            
            // Return true even if email not found for security reasons
            return true;
            
        } catch (Exception e) {
            log.error("Forgot password failed for email: {}", request.getEmail(), e);
            return false;
        }
    }
    
    private void createPasswordResetToken(Long userId, String userType, String email, String clientIp) {
        // Remove any existing tokens for this user
        passwordResetTokenRepository.deleteByUserIdAndUserType(userId, userType);
        
        // Create new token
        PasswordResetToken resetToken = new PasswordResetToken(userId, userType, clientIp);
        passwordResetTokenRepository.save(resetToken);
        
        // Send reset email
        emailService.sendPasswordResetEmail(email, resetToken.getToken(), 
            getUserNameByEmailAndType(email, userType));
    }
    
    /**
     * Reset password using token
     */
    @Transactional
    public boolean resetPassword(ResetPasswordRequest request, HttpServletRequest httpRequest) {
        try {
            Optional<PasswordResetToken> tokenOpt = passwordResetTokenRepository.findByToken(request.getToken());
            
            if (tokenOpt.isEmpty()) {
                return false;
            }
            
            PasswordResetToken resetToken = tokenOpt.get();
            
            // Check if token is expired
            if (resetToken.isExpired()) {
                passwordResetTokenRepository.delete(resetToken);
                return false;
            }
            
            // Check if too many attempts
            if (resetToken.hasTooManyAttempts()) {
                passwordResetTokenRepository.delete(resetToken);
                return false;
            }
            
            // Update password
            String salt = passwordService.generateSalt();
            String hashedPassword = passwordService.hashPassword(request.getNewPassword(), salt);
            
            if ("user".equals(resetToken.getUserType())) {
                Optional<User> userOpt = userRepository.findById(resetToken.getUserId());
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    user.setPasswordHash(hashedPassword);
                    user.setSalt(salt);
                    user.setLoginAttempts(0); // Reset login attempts
                    user.setLockedUntil(null); // Unlock account
                    userRepository.save(user);
                }
            } else if ("admin".equals(resetToken.getUserType())) {
                Optional<Admin> adminOpt = adminRepository.findById(resetToken.getUserId());
                if (adminOpt.isPresent()) {
                    Admin admin = adminOpt.get();
                    admin.setPasswordHash(hashedPassword);
                    admin.setSalt(salt);
                    admin.setLoginAttempts(0); // Reset login attempts
                    admin.setLockedUntil(null); // Unlock account
                    adminRepository.save(admin);
                }
            }
            
            // Delete the used token
            passwordResetTokenRepository.delete(resetToken);
            
            return true;
            
        } catch (Exception e) {
            log.error("Password reset failed for token: {}", request.getToken(), e);
            return false;
        }
    }
    
    /**
     * Verify email using verification token
     */
    @Transactional
    public boolean verifyEmail(String token) {
        try {
            Optional<VerificationToken> tokenOpt = verificationTokenRepository.findByToken(token);
            
            if (tokenOpt.isEmpty()) {
                return false;
            }
            
            VerificationToken verificationToken = tokenOpt.get();
            
            // Check if token is expired
            if (verificationToken.isExpired()) {
                verificationTokenRepository.delete(verificationToken);
                return false;
            }
            
            // Mark user as verified
            if ("user".equals(verificationToken.getUserType())) {
                Optional<User> userOpt = userRepository.findById(verificationToken.getUserId());
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    user.setEmailVerified(true);
                    userRepository.save(user);
                }
            }
            
            // Delete the used token
            verificationTokenRepository.delete(verificationToken);
            
            return true;
            
        } catch (Exception e) {
            log.error("Email verification failed for token: {}", token, e);
            return false;
        }
    }
    
    /**
     * Get client IP address from request
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0];
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * Helper method to get user name by email and type
     */
    private String getUserNameByEmailAndType(String email, String userType) {
        if ("user".equals(userType)) {
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                return user.getFirstName() != null ? user.getFirstName() : user.getUsername();
            }
        } else if ("admin".equals(userType)) {
            Optional<Admin> adminOpt = adminRepository.findByEmail(email);
            if (adminOpt.isPresent()) {
                Admin admin = adminOpt.get();
                return admin.getFirstName() != null ? admin.getFirstName() : admin.getUsername();
            }
        }
        return "User";
    }
}