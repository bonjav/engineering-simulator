package com.datacenterflow.project.adapter.in.web;

import com.datacenterflow.auth.domain.model.UserId;
import com.datacenterflow.project.adapter.in.web.dto.CreateProjectRequest;
import com.datacenterflow.project.adapter.in.web.dto.ProjectResponse;
import com.datacenterflow.project.adapter.in.web.dto.UpdateProjectRequest;
import com.datacenterflow.project.domain.model.ProjectId;
import com.datacenterflow.project.domain.port.in.CreateProjectUseCase;
import com.datacenterflow.project.domain.port.in.DeleteProjectUseCase;
import com.datacenterflow.project.domain.port.in.GetProjectUseCase;
import com.datacenterflow.project.domain.port.in.ListProjectsUseCase;
import com.datacenterflow.project.domain.port.in.UpdateProjectUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
@Tag(name = "Projects", description = "Project lifecycle and management")
@SecurityRequirement(name = "bearerAuth")
public class ProjectController {

    private final CreateProjectUseCase createProject;
    private final GetProjectUseCase getProject;
    private final ListProjectsUseCase listProjects;
    private final UpdateProjectUseCase updateProject;
    private final DeleteProjectUseCase deleteProject;

    public ProjectController(
        CreateProjectUseCase createProject,
        GetProjectUseCase getProject,
        ListProjectsUseCase listProjects,
        UpdateProjectUseCase updateProject,
        DeleteProjectUseCase deleteProject
    ) {
        this.createProject = createProject;
        this.getProject = getProject;
        this.listProjects = listProjects;
        this.updateProject = updateProject;
        this.deleteProject = deleteProject;
    }

    @PostMapping
    @Operation(summary = "Create a new project")
    public ResponseEntity<ProjectResponse> create(
        JwtAuthenticationToken auth,
        @Valid @RequestBody CreateProjectRequest request
    ) {
        UserId ownerId = UserId.of(auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ProjectResponse.from(createProject.createProject(ownerId, request.toCommand())));
    }

    @GetMapping
    @Operation(summary = "List all projects accessible to the current user")
    public ResponseEntity<List<ProjectResponse>> list(JwtAuthenticationToken auth) {
        UserId userId = UserId.of(auth.getName());
        return ResponseEntity.ok(
            listProjects.listAccessibleProjects(userId).stream()
                .map(ProjectResponse::from)
                .toList()
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a project by ID")
    public ResponseEntity<ProjectResponse> get(
        JwtAuthenticationToken auth,
        @PathVariable UUID id
    ) {
        UserId requesterId = UserId.of(auth.getName());
        return ResponseEntity.ok(ProjectResponse.from(
            getProject.getProject(new ProjectId(id), requesterId)
        ));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a project (OWNER or EDITOR)")
    public ResponseEntity<ProjectResponse> update(
        JwtAuthenticationToken auth,
        @PathVariable UUID id,
        @Valid @RequestBody UpdateProjectRequest request
    ) {
        UserId requesterId = UserId.of(auth.getName());
        return ResponseEntity.ok(ProjectResponse.from(
            updateProject.updateProject(new ProjectId(id), requesterId, request.toCommand())
        ));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a project (OWNER only)")
    public ResponseEntity<Void> delete(
        JwtAuthenticationToken auth,
        @PathVariable UUID id
    ) {
        UserId requesterId = UserId.of(auth.getName());
        deleteProject.deleteProject(new ProjectId(id), requesterId);
        return ResponseEntity.noContent().build();
    }
}
