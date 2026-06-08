package com.datacenterflow.project.adapter.in.web;

import com.datacenterflow.auth.domain.model.UserId;
import com.datacenterflow.project.adapter.in.web.dto.AddMemberRequest;
import com.datacenterflow.project.adapter.in.web.dto.ProjectMemberResponse;
import com.datacenterflow.project.domain.model.ProjectId;
import com.datacenterflow.project.domain.port.in.AddProjectMemberUseCase;
import com.datacenterflow.project.domain.port.in.ListProjectMembersUseCase;
import com.datacenterflow.project.domain.port.in.RemoveProjectMemberUseCase;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/members")
@Tag(name = "Project Members", description = "Manage project membership and roles")
@SecurityRequirement(name = "bearerAuth")
public class ProjectMemberController {

    private final AddProjectMemberUseCase addMember;
    private final RemoveProjectMemberUseCase removeMember;
    private final ListProjectMembersUseCase listMembers;

    public ProjectMemberController(
        AddProjectMemberUseCase addMember,
        RemoveProjectMemberUseCase removeMember,
        ListProjectMembersUseCase listMembers
    ) {
        this.addMember = addMember;
        this.removeMember = removeMember;
        this.listMembers = listMembers;
    }

    @GetMapping
    @Operation(summary = "List project members (any member)")
    public ResponseEntity<List<ProjectMemberResponse>> list(
        JwtAuthenticationToken auth,
        @PathVariable UUID projectId
    ) {
        UserId requesterId = UserId.of(auth.getName());
        return ResponseEntity.ok(
            listMembers.listMembers(new ProjectId(projectId), requesterId).stream()
                .map(ProjectMemberResponse::from)
                .toList()
        );
    }

    @PostMapping
    @Operation(summary = "Add a member to a project (OWNER only)")
    public ResponseEntity<ProjectMemberResponse> add(
        JwtAuthenticationToken auth,
        @PathVariable UUID projectId,
        @Valid @RequestBody AddMemberRequest request
    ) {
        UserId requesterId = UserId.of(auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(ProjectMemberResponse.from(
            addMember.addMember(new ProjectId(projectId), requesterId, request.toCommand())
        ));
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Remove a member from a project (OWNER only)")
    public ResponseEntity<Void> remove(
        JwtAuthenticationToken auth,
        @PathVariable UUID projectId,
        @PathVariable UUID userId
    ) {
        UserId requesterId = UserId.of(auth.getName());
        removeMember.removeMember(new ProjectId(projectId), requesterId, new UserId(userId));
        return ResponseEntity.noContent().build();
    }
}
