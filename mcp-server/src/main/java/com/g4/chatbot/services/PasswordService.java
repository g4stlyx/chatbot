package com.g4.chatbot.services;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;

@Service
public class PasswordService {
    
    @Value("${app.security.pepper}")
    private String pepper;
    
    // Increase iterations for better security (adjust based on your server performance)
    private static final int ITERATIONS = 12;
    private static final int MEMORY = 65536;    // 64MB
    private static final int PARALLELISM = 2;   // Can increase based on server CPU cores
    
    private final Argon2 argon2 = Argon2Factory.create(
            Argon2Factory.Argon2Types.ARGON2id, // Best choice for general password hashing
            32,  // Salt length
            64   // Hash length
    );
    
    // Generate a random salt
    public String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[32];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
    
    // Hash password with salt and pepper
    public String hashPassword(String password, String salt) {
        // Combine password with pepper first, then salt
        String passwordWithPepper = password + pepper;
        
        // Hash the password
        char[] passwordChars = (passwordWithPepper + salt).toCharArray();
        try {
            return argon2.hash(ITERATIONS, MEMORY, PARALLELISM, passwordChars);
        } finally {
            // Clear the password from memory for security
            for (int i = 0; i < passwordChars.length; i++) {
                passwordChars[i] = 0;
            }
        }
    }
    
    // Verify password
    public boolean verifyPassword(String password, String salt, String hash) {
        // Combine password with pepper first, then salt
        String passwordWithPepper = password + pepper;
        
        // Verify the password
        char[] passwordChars = (passwordWithPepper + salt).toCharArray();
        try {
            return argon2.verify(hash, passwordChars);
        } finally {
            // Clear the password from memory
            for (int i = 0; i < passwordChars.length; i++) {
                passwordChars[i] = 0;
            }
        }
    }
}