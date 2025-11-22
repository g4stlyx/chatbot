package com.g4.chatbot.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for authentication error statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthErrorStatisticsResponse {
    
    private long totalErrors;
    private Map<String, Long> errorTypeCounts;  // Error type -> count
    private Map<String, Long> dailyCounts;       // Date -> count (last 30 days)
    private Map<String, Long> suspiciousIps;     // IP -> count (IPs with >5 errors in 24h)
}
