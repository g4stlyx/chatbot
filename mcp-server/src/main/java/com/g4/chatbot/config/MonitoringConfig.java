package com.g4.chatbot.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class MonitoringConfig {

    /**
     * Custom health indicator for Redis connectivity
     */
    @Bean
    public HealthIndicator redisHealthIndicator(RedisTemplate<String, Object> redisTemplate) {
        return () -> {
            try {
                String result = redisTemplate.getConnectionFactory().getConnection().ping();
                if ("PONG".equals(result)) {
                    return Health.up()
                            .withDetail("redis", "Available")
                            .withDetail("connection", "Active")
                            .build();
                } else {
                    return Health.down()
                            .withDetail("redis", "Unavailable")
                            .withDetail("reason", "Ping failed")
                            .build();
                }
            } catch (Exception e) {
                return Health.down()
                        .withDetail("redis", "Unavailable")
                        .withDetail("error", e.getMessage())
                        .build();
            }
        };
    }

    /**
     * Custom health indicator for AI service connectivity
     */
    @Bean
    public HealthIndicator aiServiceHealthIndicator() {
        return () -> {
            try {
                // This would typically check AI service connectivity
                // For now, we'll just return UP
                return Health.up()
                        .withDetail("ai-service", "Available")
                        .withDetail("provider", "OpenAI/Ollama")
                        .build();
            } catch (Exception e) {
                return Health.down()
                        .withDetail("ai-service", "Unavailable")
                        .withDetail("error", e.getMessage())
                        .build();
            }
        };
    }
}