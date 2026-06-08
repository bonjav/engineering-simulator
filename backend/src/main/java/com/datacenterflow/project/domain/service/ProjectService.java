package com.datacenterflow.project.domain.service;

import com.datacenterflow.auth.domain.model.UserId;
import com.datacenterflow.project.domain.exception.ProjectAccessDeniedException;
import com.datacenterflow.project.domain.exception.ProjectNotFoundException;
import com.datacenterflow.project.domain.model.Project;
import com.datacenterflow.project.domain.model.ProjectId;
import com.datacenterflow.project.domain.model.ProjectMember;
import com.datacenterflow.project.domain.model.ProjectRole;
import com.datacenterflow.project.domain.model.ProjectStatus;
import com.datacenterflow.project.domain.port.in.AddProjectMemberUseCase;
import com.datacenterflow.project.domain.port.in.CreateProjectUseCase;
import com.datacenterflow.project.domain.port.in.DeleteProjectUseCase;
import com.datacenterflow.project.domain.port.in.GetProjectUseCase;
import com.datacenterflow.project.domain.port.in.ListProjectMembersUseCase;
import com.datacenterflow.project.domain.port.in.ListProjectsUseCase;
import com.datacenterflow.project.domain.port.in.RemoveProjectMemberUseCase;
import com.datacenterflow.project.domain.port.in.UpdateProjectUseCase;
import com.datacenterflow.project.domain.port.out.ProjectMemberRepository;
import com.datacenterflow.project.domain.port.out.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ProjectService implements
    CreateProjectUseCase,
    GetProjectUseCase,
    ListProjectsUseCase,
    UpdateProjectUseCase,
    DeleteProjectUseCase,
    AddProjectMemberUseCase,
    RemoveProjectMemberUseCase,
    ListProjectMembersUseCase {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository memberRepository;

    public ProjectService(ProjectRepository projectRepository, ProjectMemberRepository memberRepository) {
        this.projectRepository = projectRepository;
        this.memberRepository = memberRepository;
    }

    @Override
    public Project createProject(UserId ownerId, CreateProjectCommand command) {
        Instant now = Instant.now();
        ProjectId id = new ProjectId(UUID.randomUUID());
        Project project = new Project(id, command.name(), command.description(), ProjectStatus.ACTIVE, ownerId, now, now);
        Project saved = projectRepository.save(project);
        memberRepository.save(new ProjectMember(id, ownerId, ProjectRole.OWNER, now));
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Project getProject(ProjectId projectId, UserId requesterId) {
        requireAnyRole(projectId, requesterId);
        return projectRepository.findById(projectId)
            .orElseThrow(() -> new ProjectNotFoundException(projectId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Project> listAccessibleProjects(UserId userId) {
        return projectRepository.findAllAccessibleByUser(userId);
    }

    @Override
    public Project updateProject(ProjectId projectId, UserId requesterId, UpdateProjectCommand command) {
        requireEditorOrOwner(projectId, requesterId);
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new ProjectNotFoundException(projectId));
        return projectRepository.save(project.withDetails(command.name(), command.description()));
    }

    @Override
    public void deleteProject(ProjectId projectId, UserId requesterId) {
        requireOwner(projectId, requesterId);
        projectRepository.deleteById(projectId);
    }

    @Override
    public ProjectMember addMember(ProjectId projectId, UserId requesterId, AddMemberCommand command) {
        requireOwner(projectId, requesterId);
        return memberRepository.save(new ProjectMember(projectId, command.userId(), command.role(), Instant.now()));
    }

    @Override
    public void removeMember(ProjectId projectId, UserId requesterId, UserId targetUserId) {
        requireOwner(projectId, requesterId);
        // Prevent owner from removing themselves
        ProjectMember requester = getMemberOrDeny(projectId, requesterId);
        if (requester.role().isOwner() && requesterId.equals(targetUserId)) {
            throw new ProjectAccessDeniedException(requesterId, projectId);
        }
        memberRepository.delete(projectId, targetUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectMember> listMembers(ProjectId projectId, UserId requesterId) {
        requireAnyRole(projectId, requesterId);
        return memberRepository.findAllByProject(projectId);
    }

    // ── Permission helpers ────────────────────────────────────────────────────

    private ProjectMember getMemberOrDeny(ProjectId projectId, UserId userId) {
        return memberRepository.findByProjectAndUser(projectId, userId)
            .orElseThrow(() -> new ProjectAccessDeniedException(userId, projectId));
    }

    private void requireAnyRole(ProjectId projectId, UserId userId) {
        getMemberOrDeny(projectId, userId);
    }

    private void requireEditorOrOwner(ProjectId projectId, UserId userId) {
        ProjectMember member = getMemberOrDeny(projectId, userId);
        if (!member.role().canEdit()) {
            throw new ProjectAccessDeniedException(userId, projectId);
        }
    }

    private void requireOwner(ProjectId projectId, UserId userId) {
        ProjectMember member = getMemberOrDeny(projectId, userId);
        if (!member.role().isOwner()) {
            throw new ProjectAccessDeniedException(userId, projectId);
        }
    }
}
