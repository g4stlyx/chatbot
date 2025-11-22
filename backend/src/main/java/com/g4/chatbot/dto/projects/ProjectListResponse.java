package com.g4.chatbot.dto.projects;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectListResponse {
    
    private List<ProjectResponse> projects;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private int pageSize;
}
