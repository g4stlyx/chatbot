package com.g4.chatbot.dto.session;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionListResponse {
    
    private List<SessionResponse> sessions;
    private int totalPages;
    private long totalElements;
    private int currentPage;
    private int pageSize;
}
