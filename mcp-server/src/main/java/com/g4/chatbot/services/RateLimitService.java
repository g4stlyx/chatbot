package com.g4.chatbot.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.g4.chatbot.config.RateLimitConfig;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
public class RateLimitService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RateLimitConfig rateLimitConfig;

    /**
     * Check if API rate limit is exceeded for a user
     */
    public boolean isApiRateLimitExceeded(String userId) {
        String key = "api_rate_limit:" + userId;
        return isRateLimitExceeded(key, rateLimitConfig.getApiCalls(), rateLimitConfig.getApiWindow());
    }

    /**
     * Check if chat rate limit is exceeded for a user
     */
    public boolean isChatRateLimitExceeded(String userId) {
        String key = "chat_rate_limit:" + userId;
        return isRateLimitExceeded(key, rateLimitConfig.getChatRequests(), rateLimitConfig.getChatWindow());
    }

    /**
     * Check if login rate limit is exceeded for an IP
     */
    public boolean isLoginRateLimitExceeded(String ipAddress) {
        String key = "login_rate_limit:" + ipAddress;
        return isRateLimitExceeded(key, rateLimitConfig.getLoginAttempts(), rateLimitConfig.getLoginWindow());
    }

    /**
     * Generic rate limiting using sliding window counter
     */
    private boolean isRateLimitExceeded(String key, int maxRequests, long windowMs) {
        try {
            String countStr = (String) redisTemplate.opsForValue().get(key);
            int currentCount = countStr != null ? Integer.parseInt(countStr) : 0;

            if (currentCount >= maxRequests) {
                return true; // Rate limit exceeded
            }

            // Increment counter
            if (currentCount == 0) {
                // First request in window - set with expiration
                redisTemplate.opsForValue().set(key, "1", Duration.ofMillis(windowMs));
            } else {
                // Increment existing counter
                redisTemplate.opsForValue().increment(key);
            }

            return false; // Within limit
        } catch (Exception e) {
            // On Redis failure, allow the request (fail open)
            return false;
        }
    }

    /**
     * Reset rate limit for a key (useful for testing or manual reset)
     */
    public void resetRateLimit(String key) {
        redisTemplate.delete(key);
    }

    /**
     * Get remaining requests for a key
     */
    public int getRemainingRequests(String key, int maxRequests) {
        try {
            String countStr = (String) redisTemplate.opsForValue().get(key);
            int currentCount = countStr != null ? Integer.parseInt(countStr) : 0;
            return Math.max(0, maxRequests - currentCount);
        } catch (Exception e) {
            return maxRequests; // On error, assume no requests made
        }
    }

    /**
     * Get TTL for a rate limit key
     */
    public long getTTL(String key) {
        try {
            return redisTemplate.getExpire(key, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            return -1; // Error or no expiration
        }
    }
}