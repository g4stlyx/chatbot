package com.g4.chatbot.dto.projects;

import com.g4.chatbot.models.Project;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {
    
    private Long id;
    private Long userId;
    private String name;
    private String description;
    private String color;
    private String icon;
    private Boolean isArchived;
    private Integer sessionCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static ProjectResponse from(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .userId(project.getUserId())
                .name(project.getName())
                .description(project.getDescription())
                .color(project.getColor())
                .icon(project.getIcon())
                .isArchived(project.getIsArchived())
                .sessionCount(project.getSessionCount())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }
}
