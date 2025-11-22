package com.g4.chatbot.dto.projects;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateProjectRequest {
    
    @NotBlank(message = "Project name is required")
    @Size(min = 1, max = 100, message = "Project name must be between 1 and 100 characters")
    private String name;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    @Size(min = 7, max = 7, message = "Color must be a valid hex code (e.g., #3B82F6)")
    private String color;
    
    @Size(max = 50, message = "Icon name must not exceed 50 characters")
    private String icon;
}
