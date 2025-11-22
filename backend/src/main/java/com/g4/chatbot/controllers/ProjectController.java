package com.g4.chatbot.controllers;

import com.g4.chatbot.dto.projects.CreateProjectRequest;
import com.g4.chatbot.dto.projects.ProjectListResponse;
import com.g4.chatbot.dto.projects.ProjectResponse;
import com.g4.chatbot.dto.projects.UpdateProjectRequest;
import com.g4.chatbot.services.ProjectService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for managing projects (user endpoints)
 */
@RestController
@RequestMapping("/api/v1/projects")
@Slf4j
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public class ProjectController {
    
    @Autowired
    private ProjectService projectService;
    
    /**
     * POST /api/v1/projects
     * Create a new project
     */
    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody CreateProjectRequest request,
            Authentication authentication) {
        
        Long userId = (Long) authentication.getDetails();
        log.info("User {} creating project: {}", userId, request.getName());
        
        ProjectResponse response = projectService.createProject(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * GET /api/v1/projects
     * Get all projects for current user
     */
    @GetMapping
    public ResponseEntity<ProjectListResponse> getUserProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) Boolean archived,
            Authentication authentication) {
        
        Long userId = (Long) authentication.getDetails();
        log.info("User {} fetching projects", userId);
        
        ProjectListResponse response = projectService.getUserProjects(
                userId, page, size, sortBy, sortDirection, archived
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/v1/projects/{projectId}
     * Get project by ID
     */
    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> getProjectById(
            @PathVariable Long projectId,
            Authentication authentication) {
        
        Long userId = (Long) authentication.getDetails();
        log.info("User {} fetching project {}", userId, projectId);
        
        ProjectResponse response = projectService.getProjectById(userId, projectId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * PUT /api/v1/projects/{projectId}
     * Update project
     */
    @PutMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable Long projectId,
            @Valid @RequestBody UpdateProjectRequest request,
            Authentication authentication) {
        
        Long userId = (Long) authentication.getDetails();
        log.info("User {} updating project {}", userId, projectId);
        
        ProjectResponse response = projectService.updateProject(userId, projectId, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * POST /api/v1/projects/{projectId}/archive
     * Archive project
     */
    @PostMapping("/{projectId}/archive")
    public ResponseEntity<ProjectResponse> archiveProject(
            @PathVariable Long projectId,
            Authentication authentication) {
        
        Long userId = (Long) authentication.getDetails();
        log.info("User {} archiving project {}", userId, projectId);
        
        ProjectResponse response = projectService.toggleArchiveProject(userId, projectId, true);
        return ResponseEntity.ok(response);
    }
    
    /**
     * POST /api/v1/projects/{projectId}/unarchive
     * Unarchive project
     */
    @PostMapping("/{projectId}/unarchive")
    public ResponseEntity<ProjectResponse> unarchiveProject(
            @PathVariable Long projectId,
            Authentication authentication) {
        
        Long userId = (Long) authentication.getDetails();
        log.info("User {} unarchiving project {}", userId, projectId);
        
        ProjectResponse response = projectService.toggleArchiveProject(userId, projectId, false);
        return ResponseEntity.ok(response);
    }
    
    /**
     * DELETE /api/v1/projects/{projectId}
     * Delete project
     */
    @DeleteMapping("/{projectId}")
    public ResponseEntity<?> deleteProject(
            @PathVariable Long projectId,
            Authentication authentication) {
        
        Long userId = (Long) authentication.getDetails();
        log.info("User {} deleting project {}", userId, projectId);
        
        projectService.deleteProject(userId, projectId);
        return ResponseEntity.ok(Map.of("message", "Project deleted successfully"));
    }
    
    /**
     * POST /api/v1/projects/{projectId}/sessions/{sessionId}
     * Add chat session to project
     */
    @PostMapping("/{projectId}/sessions/{sessionId}")
    public ResponseEntity<?> addSessionToProject(
            @PathVariable Long projectId,
            @PathVariable String sessionId,
            Authentication authentication) {
        
        Long userId = (Long) authentication.getDetails();
        log.info("User {} adding session {} to project {}", userId, sessionId, projectId);
        
        projectService.addSessionToProject(userId, projectId, sessionId);
        return ResponseEntity.ok(Map.of("message", "Session added to project successfully"));
    }
    
    /**
     * DELETE /api/v1/projects/sessions/{sessionId}
     * Remove chat session from project
     */
    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<?> removeSessionFromProject(
            @PathVariable String sessionId,
            Authentication authentication) {
        
        Long userId = (Long) authentication.getDetails();
        log.info("User {} removing session {} from project", userId, sessionId);
        
        projectService.removeSessionFromProject(userId, sessionId);
        return ResponseEntity.ok(Map.of("message", "Session removed from project successfully"));
    }
    
    /**
     * GET /api/v1/projects/search
     * Search projects by name
     */
    @GetMapping("/search")
    public ResponseEntity<ProjectListResponse> searchProjects(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        
        Long userId = (Long) authentication.getDetails();
        log.info("User {} searching projects with query: {}", userId, query);
        
        ProjectListResponse response = projectService.searchProjects(userId, query, page, size);
        return ResponseEntity.ok(response);
    }
}
