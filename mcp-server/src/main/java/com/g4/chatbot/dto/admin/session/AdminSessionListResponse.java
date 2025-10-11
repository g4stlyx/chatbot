package com.g4.chatbot.dto.admin.session;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Paginated response for admin chat session list
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminSessionListResponse {
    
    private List<AdminChatSessionDTO> sessions;
    private Integer currentPage;
    private Integer totalPages;
    private Long totalElements;
    private Integer pageSize;
}
