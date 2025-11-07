package com.g4.chatbot.dto.admin.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Paginated response for admin message list
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminMessageListResponse {
    
    private List<AdminMessageDTO> messages;
    private Integer currentPage;
    private Integer totalPages;
    private Long totalElements;
    private Integer pageSize;
}
