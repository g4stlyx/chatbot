package com.g4.chatbot.services;

import com.g4.chatbot.dto.projects.CreateProjectRequest;
import com.g4.chatbot.dto.projects.ProjectListResponse;
import com.g4.chatbot.dto.projects.ProjectResponse;
import com.g4.chatbot.dto.projects.UpdateProjectRequest;
import com.g4.chatbot.exception.BadRequestException;
import com.g4.chatbot.exception.ResourceNotFoundException;
import com.g4.chatbot.exception.UnauthorizedException;
import com.g4.chatbot.models.ChatSession;
import com.g4.chatbot.models.Project;
import com.g4.chatbot.repos.ChatSessionRepository;
import com.g4.chatbot.repos.ProjectRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProjectService {
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private ChatSessionRepository chatSessionRepository;
    
    /**
     * Create a new project
     */
    @Transactional
    public ProjectResponse createProject(Long userId, CreateProjectRequest request) {
        log.info("Creating project for user {}: {}", userId, request.getName());
        
        // Check if project name already exists for user
        if (projectRepository.existsByUserIdAndName(userId, request.getName())) {
            throw new BadRequestException("Project with name '" + request.getName() + "' already exists");
        }
        
        Project project = Project.builder()
                .userId(userId)
                .name(request.getName())
                .description(request.getDescription())
                .color(request.getColor() != null ? request.getColor() : "#3B82F6")
                .icon(request.getIcon() != null ? request.getIcon() : "folder")
                .isArchived(false)
                .sessionCount(0)
                .build();
        
        Project savedProject = projectRepository.save(project);
        log.info("Project created successfully with ID: {}", savedProject.getId());
        
        return ProjectResponse.from(savedProject);
    }
    
    /**
     * Get all projects for a user with pagination
     */
    @Transactional(readOnly = true)
    public ProjectListResponse getUserProjects(
            Long userId,
            int page,
            int size,
            String sortBy,
            String sortDirection,
            Boolean archived) {
        
        log.info("Fetching projects for user {}, page: {}, size: {}", userId, page, size);
        
        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Project> projectsPage;
        if (archived != null) {
            if (archived) {
                projectsPage = projectRepository.findByUserIdAndIsArchivedTrue(userId, pageable);
            } else {
                projectsPage = projectRepository.findByUserIdAndIsArchivedFalse(userId, pageable);
            }
        } else {
            projectsPage = projectRepository.findByUserId(userId, pageable);
        }
        
        List<ProjectResponse> projects = projectsPage.getContent().stream()
                .map(ProjectResponse::from)
                .collect(Collectors.toList());
        
        return ProjectListResponse.builder()
                .projects(projects)
                .currentPage(projectsPage.getNumber())
                .totalPages(projectsPage.getTotalPages())
                .totalElements(projectsPage.getTotalElements())
                .pageSize(projectsPage.getSize())
                .build();
    }
    
    /**
     * Get project by ID
     */
    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long userId, Long projectId) {
        log.info("Fetching project {} for user {}", projectId, userId);
        
        Project project = projectRepository.findByIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found or access denied"));
        
        return ProjectResponse.from(project);
    }
    
    /**
     * Update project
     */
    @Transactional
    public ProjectResponse updateProject(Long userId, Long projectId, UpdateProjectRequest request) {
        log.info("Updating project {} for user {}", projectId, userId);
        
        Project project = projectRepository.findByIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found or access denied"));
        
        // Check if new name conflicts with existing project
        if (request.getName() != null && !request.getName().equals(project.getName())) {
            if (projectRepository.existsByUserIdAndName(userId, request.getName())) {
                throw new BadRequestException("Project with name '" + request.getName() + "' already exists");
            }
            project.setName(request.getName());
        }
        
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }
        
        if (request.getColor() != null) {
            project.setColor(request.getColor());
        }
        
        if (request.getIcon() != null) {
            project.setIcon(request.getIcon());
        }
        
        Project updatedProject = projectRepository.save(project);
        log.info("Project {} updated successfully", projectId);
        
        return ProjectResponse.from(updatedProject);
    }
    
    /**
     * Archive/Unarchive project
     */
    @Transactional
    public ProjectResponse toggleArchiveProject(Long userId, Long projectId, boolean archive) {
        log.info("{} project {} for user {}", archive ? "Archiving" : "Unarchiving", projectId, userId);
        
        Project project = projectRepository.findByIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found or access denied"));
        
        project.setIsArchived(archive);
        Project updatedProject = projectRepository.save(project);
        
        log.info("Project {} {} successfully", projectId, archive ? "archived" : "unarchived");
        
        return ProjectResponse.from(updatedProject);
    }
    
    /**
     * Delete project
     */
    @Transactional
    public void deleteProject(Long userId, Long projectId) {
        log.info("Deleting project {} for user {}", projectId, userId);
        
        Project project = projectRepository.findByIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found or access denied"));
        
        // Remove project reference from all chat sessions
        List<ChatSession> sessions = chatSessionRepository.findByProjectId(projectId);
        for (ChatSession session : sessions) {
            session.setProjectId(null);
        }
        chatSessionRepository.saveAll(sessions);
        
        projectRepository.delete(project);
        log.info("Project {} deleted successfully", projectId);
    }
    
    /**
     * Add chat session to project
     */
    @Transactional
    public void addSessionToProject(Long userId, Long projectId, String sessionId) {
        log.info("Adding session {} to project {} for user {}", sessionId, projectId, userId);
        
        // Verify project belongs to user
        Project project = projectRepository.findByIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found or access denied"));
        
        // Verify session belongs to user
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found"));
        
        if (!session.getUserId().equals(userId)) {
            throw new UnauthorizedException("You don't have permission to modify this session");
        }
        
        // Remove from old project if exists
        if (session.getProjectId() != null) {
            projectRepository.decrementSessionCount(session.getProjectId());
        }
        
        // Add to new project
        session.setProjectId(projectId);
        chatSessionRepository.save(session);
        projectRepository.incrementSessionCount(projectId);
        
        log.info("Session {} added to project {} successfully", sessionId, projectId);
    }
    
    /**
     * Remove chat session from project
     */
    @Transactional
    public void removeSessionFromProject(Long userId, String sessionId) {
        log.info("Removing session {} from project for user {}", sessionId, userId);
        
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found"));
        
        if (!session.getUserId().equals(userId)) {
            throw new UnauthorizedException("You don't have permission to modify this session");
        }
        
        if (session.getProjectId() != null) {
            projectRepository.decrementSessionCount(session.getProjectId());
            session.setProjectId(null);
            chatSessionRepository.save(session);
            log.info("Session {} removed from project successfully", sessionId);
        }
    }
    
    /**
     * Search projects by name
     */
    @Transactional(readOnly = true)
    public ProjectListResponse searchProjects(Long userId, String query, int page, int size) {
        log.info("Searching projects for user {} with query: {}", userId, query);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Project> projectsPage = projectRepository.searchByName(userId, query, pageable);
        
        List<ProjectResponse> projects = projectsPage.getContent().stream()
                .map(ProjectResponse::from)
                .collect(Collectors.toList());
        
        return ProjectListResponse.builder()
                .projects(projects)
                .currentPage(projectsPage.getNumber())
                .totalPages(projectsPage.getTotalPages())
                .totalElements(projectsPage.getTotalElements())
                .pageSize(projectsPage.getSize())
                .build();
    }
}
