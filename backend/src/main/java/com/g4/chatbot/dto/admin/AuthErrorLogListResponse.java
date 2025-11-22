package com.g4.chatbot.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for paginated list of authentication error logs
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthErrorLogListResponse {
    
    private List<AuthErrorLogResponse> logs;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private int pageSize;
}
