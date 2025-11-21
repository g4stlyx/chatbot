package com.g4.chatbot.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for paginated prompt injection log list responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptInjectionLogListResponse {
    
    private List<PromptInjectionLogResponse> logs;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private int pageSize;
}
