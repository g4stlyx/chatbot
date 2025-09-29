package com.g4.chatbot.services;

import com.g4.chatbot.dto.AuthResponse;
import com.g4.chatbot.dto.LoginRequest;
import com.g4.chatbot.dto.RegisterRequest;
import com.g4.chatbot.models.Admin;
import com.g4.chatbot.models.Doctor;
import com.g4.chatbot.models.Nurse;
import com.g4.chatbot.models.VerificationToken;
import com.g4.chatbot.repos.AdminRepository;
import com.g4.chatbot.repos.DoctorRepository;
import com.g4.chatbot.repos.NurseRepository;
import com.g4.chatbot.repos.PolyclinicRepository;
import com.g4.chatbot.repos.VerificationTokenRepository;
import com.g4.chatbot.security.JwtUtils;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;

import java.util.Optional;

//! on register: email/username kullanılıyor demek çok güvenli olmayabilir.
//! dememek de farklı bir sıkıntı, kullanıcı kayıt olurken sorun nerede anlamayacaktır.

@Service
public class AuthService {

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private NurseRepository nurseRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PolyclinicRepository polyclinicRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AuthLogService authLogService;

    @Autowired
    private FcmTokenService fcmTokenService;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private AccountSecurityService accountSecurityService;

    @Autowired
    private SecurityDelayService securityDelayService;

    public AuthResponse register(RegisterRequest request, HttpServletRequest httpRequest) {
        // First, validate the polyclinic exists
        if (!polyclinicRepository.existsById(request.getPolyclinicId())) {
            // Log the error
            authLogService.logAuthentication(
                    httpRequest,
                    0,
                    request.getUserType(),
                    false,
                    "Registration failed: Polyclinic ID " + request.getPolyclinicId() + " does not exist");
            throw new IllegalArgumentException("Polyclinic with ID " + request.getPolyclinicId() + " does not exist");
        }

        // Check if username already exists (across all user types)
        if (nurseRepository.existsByUsername(request.getUsername()) ||
                doctorRepository.existsByUsername(request.getUsername()) ||
                adminRepository.existsByUsername(request.getUsername())) {
            authLogService.logAuthentication(
                    httpRequest,
                    0,
                    request.getUserType(),
                    false,
                    "Registration failed: Username " + request.getUsername() + " already exists");
            throw new IllegalArgumentException(
                    "Invalid registration information. Please try again with different credentials.");
        }

        // Check if email already exists (across all user types)
        if (nurseRepository.existsByEmail(request.getEmail()) ||
                doctorRepository.existsByEmail(request.getEmail())) {
            authLogService.logAuthentication(
                    httpRequest,
                    0,
                    request.getUserType(),
                    false,
                    "Registration failed: Email " + request.getEmail() + " already exists");
            throw new IllegalArgumentException(
                    "Invalid registration information. Please try again with different credentials.");
        }

        // Check if nurse code already exists (only for nurse registration)
        if ("nurse".equals(request.getUserType().toLowerCase()) && request.getNurseCode() != null) {
            boolean nurseCodeExists = nurseRepository.existsByNurseCode(request.getNurseCode());
            if (nurseCodeExists) {
                authLogService.logAuthentication(
                        httpRequest,
                        0,
                        request.getUserType(),
                        false,
                        "Registration failed: Nurse code " + request.getNurseCode() + " already exists");
                throw new IllegalArgumentException(
                        "Invalid registration information. Please try again with different credentials.");
            }
        }

        String salt = passwordService.generateSalt();
        String hashedPassword = passwordService.hashPassword(request.getPassword(), salt);

        try {
            if ("nurse".equals(request.getUserType().toLowerCase())) {
                Nurse nurse = new Nurse();
                nurse.setName(request.getName());
                nurse.setEmail(request.getEmail());
                nurse.setPhoneNumber(request.getPhoneNumber());
                nurse.setUsername(request.getUsername());
                nurse.setPassword(hashedPassword);
                nurse.setSalt(salt);
                nurse.setNurseCode(request.getNurseCode());
                nurse.setPolyclinicId(request.getPolyclinicId());
                nurse.setLevel(2); // Default level for nurse
                nurse.setIsVerified(false);

                nurse = nurseRepository.save(nurse);

                // Create verification token
                VerificationToken verificationToken = new VerificationToken();
                verificationToken.setUserId(nurse.getId());
                verificationToken.setUserType("nurse");
                verificationToken = verificationTokenRepository.save(verificationToken);

                // Send verification email
                try {
                    emailService.sendVerificationEmail(
                            nurse.getEmail(),
                            verificationToken.getToken(),
                            nurse.getName());
                } catch (Exception e) {
                    System.err.println("Warning: Failed to send verification email: " + e.getMessage());
                    // Continue with registration even if email fails
                }

                // Register FCM token if provided in the request
                if (request.getFcmToken() != null && !request.getFcmToken().isEmpty()) {
                    try {
                        // Get polyclinic name
                        String polyclinicName = polyclinicRepository.findById(request.getPolyclinicId())
                                .map(polyclinic -> polyclinic.getName())
                                .orElse("Unknown Polyclinic");

                        // Register the token
                        FcmToken fcmTokenEntity = fcmTokenService.registerToken(
                                request.getFcmToken(),
                                "nurse",
                                polyclinicName);

                        // Update the nurse with the token ID
                        if (fcmTokenEntity != null) {
                            nurse.setFcmTokenId(fcmTokenEntity.getId());
                            nurse = nurseRepository.save(nurse);
                        }
                    } catch (Exception e) {
                        // Log but don't fail registration if token registration fails
                        System.err.println("Warning: FCM token registration failed: " + e.getMessage());
                    }
                }

                // Log successful registration
                authLogService.logAuthentication(
                        httpRequest,
                        nurse.getId(),
                        "nurse",
                        true,
                        "Registration successful");

                // Generate JWT token
                String token = jwtUtils.generateToken(nurse.getUsername(), nurse.getId(), "nurse");
                String fcmToken = fcmTokenService.getFcmTokenForUser(nurse.getId(), "nurse");

                return new AuthResponse(
                        nurse.getId(),
                        nurse.getName(),
                        nurse.getUsername(),
                        "nurse",
                        token,
                        nurse.getPolyclinicId(),
                        fcmToken);
            } else if ("doctor".equals(request.getUserType().toLowerCase())) {
                Doctor doctor = new Doctor();
                doctor.setName(request.getName());
                doctor.setEmail(request.getEmail());
                doctor.setPhoneNumber(request.getPhoneNumber());
                doctor.setUsername(request.getUsername());
                doctor.setPassword(hashedPassword);
                doctor.setSalt(salt);
                doctor.setPolyclinicId(request.getPolyclinicId());
                doctor.setIsVerified(false);

                doctor = doctorRepository.save(doctor);

                // Create verification token
                VerificationToken verificationToken = new VerificationToken();
                verificationToken.setUserId(doctor.getId());
                verificationToken.setUserType("doctor");
                verificationToken = verificationTokenRepository.save(verificationToken);

                // Send verification email
                try {
                    emailService.sendVerificationEmail(
                            doctor.getEmail(),
                            verificationToken.getToken(),
                            doctor.getName());
                } catch (Exception e) {
                    System.err.println("Warning: Failed to send verification email: " + e.getMessage());
                    // Continue with registration even if email fails
                }

                // Register FCM token if provided in the request
                if (request.getFcmToken() != null && !request.getFcmToken().isEmpty()) {
                    try {
                        // Get polyclinic name
                        String polyclinicName = polyclinicRepository.findById(request.getPolyclinicId())
                                .map(polyclinic -> polyclinic.getName())
                                .orElse("Unknown Polyclinic");

                        // Register the token
                        FcmToken fcmTokenEntity = fcmTokenService.registerToken(
                                request.getFcmToken(),
                                "doctor",
                                polyclinicName);

                        // Update the nurse with the token ID
                        if (fcmTokenEntity != null) {
                            doctor.setFcmTokenId(fcmTokenEntity.getId());
                            doctor = doctorRepository.save(doctor);
                        }
                    } catch (Exception e) {
                        // Log but don't fail registration if token registration fails
                        System.err.println("Warning: FCM token registration failed: " + e.getMessage());
                    }
                }

                // Log successful registration
                authLogService.logAuthentication(
                        httpRequest,
                        doctor.getId(),
                        "doctor",
                        true,
                        "Registration successful");

                // Generate JWT token
                String token = jwtUtils.generateToken(doctor.getUsername(), doctor.getId(), "doctor");
                String fcmToken = fcmTokenService.getFcmTokenForUser(doctor.getId(), "doctor");

                return new AuthResponse(
                        doctor.getId(),
                        doctor.getName(),
                        doctor.getUsername(),
                        "doctor",
                        token,
                        doctor.getPolyclinicId(),
                        fcmToken);
            } else {
                // Log failed registration - invalid user type
                authLogService.logAuthentication(
                        httpRequest,
                        0,
                        request.getUserType(),
                        false,
                        "Invalid user type");
                throw new IllegalArgumentException("Invalid user type. Must be 'nurse' or 'doctor'");
            }
        } catch (Exception e) {
            // Log registration failure
            authLogService.logAuthentication(
                    httpRequest,
                    0,
                    request.getUserType(),
                    false,
                    "Registration failed: " + e.getMessage());
            throw e;
        }
    }

    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        // Check for suspicious activity first
        if (authLogService.isIpSuspicious(httpRequest)) {
            // Log the suspicious activity
            authLogService.logAuthentication(
                    httpRequest,
                    0,
                    request.getUserType(),
                    false,
                    "IP temporarily blocked due to too many failed attempts");
            throw new LockedException("Too many failed login attempts. Please try again later.");
        }

        if ("nurse".equals(request.getUserType().toLowerCase())) {
            Optional<Nurse> nurseOpt = nurseRepository.findByUsername(request.getUsername());
            if (nurseOpt.isPresent()) {
                Nurse nurse = nurseOpt.get();

                // Check if account is locked
                if (accountSecurityService.checkAndUpdateLockStatus(nurse.getId(), "nurse",
                        httpRequest.getRemoteAddr())) {
                    authLogService.logAuthentication(
                            httpRequest,
                            nurse.getId(),
                            "nurse",
                            false,
                            "Login failed: Account is locked due to too many failed attempts");
                    throw new LockedException(
                            "Your account is temporarily locked due to too many failed login attempts. Please try again later.");
                }

                // Check if the account is verified
                if (!nurse.getIsVerified()) {
                    // Log failed login - account not verified
                    authLogService.logAuthentication(
                            httpRequest,
                            nurse.getId(),
                            "nurse",
                            false,
                            "Login failed: Account not verified");
                    throw new LockedException("Please verify your email address before logging in");
                }

                if (passwordService.verifyPassword(request.getPassword(), nurse.getSalt(), nurse.getPassword())) {
                    // Record successful login to reset failed attempts counter
                    accountSecurityService.recordLoginSuccess(nurse.getId(), "nurse");

                    // Log successful login
                    authLogService.logAuthentication(
                            httpRequest,
                            nurse.getId(),
                            "nurse",
                            true,
                            "Login successful");

                    // Generate JWT token
                    String token = jwtUtils.generateToken(nurse.getUsername(), nurse.getId(), "nurse");
                    String fcmToken = fcmTokenService.getFcmTokenForUser(nurse.getId(), "nurse");

                    return new AuthResponse(
                            nurse.getId(),
                            nurse.getName(),
                            nurse.getUsername(),
                            "nurse",
                            token,
                            nurse.getPolyclinicId(),
                            fcmToken);
                } else {
                    // Record failed login attempt
                    accountSecurityService.recordLoginFailure(nurse.getId(), "nurse", httpRequest.getRemoteAddr());

                    // Log failed login - incorrect password
                    authLogService.logAuthentication(
                            httpRequest,
                            nurse.getId(),
                            "nurse",
                            false,
                            "Incorrect password");
                }
            } else {
                // Log failed login - user not found
                authLogService.logAuthentication(
                        httpRequest,
                        0,
                        "nurse",
                        false,
                        "Username not found: " + request.getUsername());
            }
        } else if ("doctor".equals(request.getUserType().toLowerCase())) {
            Optional<Doctor> doctorOpt = doctorRepository.findByUsername(request.getUsername());
            if (doctorOpt.isPresent()) {
                Doctor doctor = doctorOpt.get();

                // Check if account is locked
                if (accountSecurityService.checkAndUpdateLockStatus(doctor.getId(), "doctor",
                        httpRequest.getRemoteAddr())) {
                    authLogService.logAuthentication(
                            httpRequest,
                            doctor.getId(),
                            "doctor",
                            false,
                            "Login failed: Account is locked due to too many failed attempts");
                    throw new LockedException(
                            "Your account is temporarily locked due to too many failed login attempts. Please try again later.");
                }

                // Check if the account is verified
                if (!doctor.getIsVerified()) {
                    // Log failed login - account not verified
                    authLogService.logAuthentication(
                            httpRequest,
                            doctor.getId(),
                            "doctor",
                            false,
                            "Login failed: Account not verified");
                    throw new LockedException("Please verify your email address before logging in");
                }

                if (passwordService.verifyPassword(request.getPassword(), doctor.getSalt(), doctor.getPassword())) {
                    // Record successful login to reset failed attempts counter
                    accountSecurityService.recordLoginSuccess(doctor.getId(), "doctor");
                    // Log successful login
                    authLogService.logAuthentication(
                            httpRequest,
                            doctor.getId(),
                            "doctor",
                            true,
                            "Login successful");

                    // Generate JWT token
                    String token = jwtUtils.generateToken(doctor.getUsername(), doctor.getId(), "doctor");
                    String fcmToken = fcmTokenService.getFcmTokenForUser(doctor.getId(), "doctor");

                    return new AuthResponse(
                            doctor.getId(),
                            doctor.getName(),
                            doctor.getUsername(),
                            "doctor",
                            token,
                            doctor.getPolyclinicId(),
                            fcmToken);
                } else {
                    // Record failed login attempt
                    accountSecurityService.recordLoginFailure(doctor.getId(), "doctor", httpRequest.getRemoteAddr());

                    // Log failed login - incorrect password
                    authLogService.logAuthentication(
                            httpRequest,
                            doctor.getId(),
                            "doctor",
                            false,
                            "Incorrect password");
                }
            } else {
                // Log failed login - user not found
                authLogService.logAuthentication(
                        httpRequest,
                        0,
                        "doctor",
                        false,
                        "Username not found: " + request.getUsername());
            }
        } else if ("admin".equals(request.getUserType().toLowerCase())) {
            Optional<Admin> adminOpt = adminRepository.findByUsername(request.getUsername());
            if (adminOpt.isPresent()) {
                Admin admin = adminOpt.get();

                // Check if account is locked
                if (accountSecurityService.checkAndUpdateLockStatus(admin.getId(), "admin",
                        httpRequest.getRemoteAddr())) {
                    authLogService.logAuthentication(
                            httpRequest,
                            admin.getId(),
                            "admin",
                            false,
                            "Login failed: Account is locked due to too many failed attempts");
                    throw new LockedException(
                            "Your account is temporarily locked due to too many failed login attempts. Please try again later.");
                }

                if (passwordService.verifyPassword(request.getPassword(), admin.getSalt(), admin.getPassword())) {
                    // Record successful login to reset failed attempts counter
                    accountSecurityService.recordLoginSuccess(admin.getId(), "admin");
                    // Log successful login
                    authLogService.logAuthentication(
                            httpRequest,
                            admin.getId(),
                            "admin",
                            true,
                            "Login successful");

                    // Generate JWT token with admin level
                    String token = jwtUtils.generateToken(admin.getUsername(), admin.getId(), "admin", admin.getLevel());

                    return new AuthResponse(
                            admin.getId(),
                            admin.getUsername(),
                            admin.getUsername(),
                            "admin",
                            token,
                            0,
                            null,
                            admin.getLevel());
                } else {
                    // Record failed login attempt
                    accountSecurityService.recordLoginFailure(admin.getId(), "admin", httpRequest.getRemoteAddr());

                    // Log failed login - incorrect password
                    authLogService.logAuthentication(
                            httpRequest,
                            admin.getId(),
                            "admin",
                            false,
                            "Incorrect password");
                }
            } else {
                // Log failed login - user not found
                authLogService.logAuthentication(
                        httpRequest,
                        0,
                        "admin",
                        false,
                        "Username not found: " + request.getUsername());
            }
        }

        // Add this to prevent timing attacks
        securityDelayService.applyRandomDelay();

        // If we get here, authentication failed
        throw new BadCredentialsException("Invalid username or password");
    }

    /**
     * Verify if a password is valid for a given username and role.
     * 
     * @param username    the username to check
     * @param role        the role of the user (nurse, doctor, admin)
     * @param password    the password to verify
     * @param httpRequest the HTTP request
     * @return true if the password is valid, false otherwise
     */
    public boolean verifyPassword(String username, String role, String password, HttpServletRequest httpRequest) {
        try {
            if ("nurse".equals(role.toLowerCase())) {
                Optional<Nurse> nurseOpt = nurseRepository.findByUsername(username);
                if (nurseOpt.isPresent()) {
                    Nurse nurse = nurseOpt.get();
                    boolean valid = passwordService.verifyPassword(password, nurse.getSalt(), nurse.getPassword());

                    // Log the verification attempt
                    authLogService.logAuthentication(
                            httpRequest,
                            nurse.getId(),
                            "nurse",
                            valid,
                            valid ? "Password verification successful" : "Password verification failed");

                    return valid;
                }
            } else if ("doctor".equals(role.toLowerCase())) {
                Optional<Doctor> doctorOpt = doctorRepository.findByUsername(username);
                if (doctorOpt.isPresent()) {
                    Doctor doctor = doctorOpt.get();
                    boolean valid = passwordService.verifyPassword(password, doctor.getSalt(), doctor.getPassword());

                    // Log the verification attempt
                    authLogService.logAuthentication(
                            httpRequest,
                            doctor.getId(),
                            "doctor",
                            valid,
                            valid ? "Password verification successful" : "Password verification failed");

                    return valid;
                }
            } else if ("admin".equals(role.toLowerCase())) {
                Optional<Admin> adminOpt = adminRepository.findByUsername(username);
                if (adminOpt.isPresent()) {
                    Admin admin = adminOpt.get();
                    boolean valid = passwordService.verifyPassword(password, admin.getSalt(), admin.getPassword());

                    // Log the verification attempt
                    authLogService.logAuthentication(
                            httpRequest,
                            admin.getId(),
                            "admin",
                            valid,
                            valid ? "Password verification successful" : "Password verification failed");

                    return valid;
                }
            }

            // Add delay to prevent timing attacks
            securityDelayService.applyRandomDelay();

            // User not found
            authLogService.logAuthentication(
                    httpRequest,
                    0,
                    role,
                    false,
                    "Password verification failed: User not found");

            return false;
        } catch (Exception e) {
            // Log error
            authLogService.logAuthentication(
                    httpRequest,
                    0,
                    role,
                    false,
                    "Password verification error: " + e.getMessage());

            return false;
        }
    }
}