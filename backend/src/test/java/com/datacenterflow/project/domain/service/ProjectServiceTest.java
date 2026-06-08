package com.datacenterflow.project.domain.service;

import com.datacenterflow.auth.domain.model.UserId;
import com.datacenterflow.project.domain.exception.ProjectAccessDeniedException;
import com.datacenterflow.project.domain.exception.ProjectNotFoundException;
import com.datacenterflow.project.domain.model.Project;
import com.datacenterflow.project.domain.model.ProjectId;
import com.datacenterflow.project.domain.model.ProjectMember;
import com.datacenterflow.project.domain.model.ProjectRole;
import com.datacenterflow.project.domain.model.ProjectStatus;
import com.datacenterflow.project.domain.port.in.AddProjectMemberUseCase.AddMemberCommand;
import com.datacenterflow.project.domain.port.in.CreateProjectUseCase.CreateProjectCommand;
import com.datacenterflow.project.domain.port.in.UpdateProjectUseCase.UpdateProjectCommand;
import com.datacenterflow.project.domain.port.out.ProjectMemberRepository;
import com.datacenterflow.project.domain.port.out.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock ProjectRepository projectRepository;
    @Mock ProjectMemberRepository memberRepository;

    private ProjectService service;
    private final UserId owner = new UserId(UUID.randomUUID());
    private final UserId editor = new UserId(UUID.randomUUID());
    private final UserId viewer = new UserId(UUID.randomUUID());
    private final UserId stranger = new UserId(UUID.randomUUID());

    @BeforeEach
    void setUp() {
        service = new ProjectService(projectRepository, memberRepository);
    }

    @Test
    void createProject_savesProjectAndOwnerMembership() {
        Project saved = project(new ProjectId(UUID.randomUUID()), owner);
        given(projectRepository.save(any())).willReturn(saved);
        given(memberRepository.save(any())).willReturn(ownerMember(saved.id()));

        Project result = service.createProject(owner, new CreateProjectCommand("Test", "Desc"));

        assertThat(result).isNotNull();
        verify(projectRepository).save(any());
        verify(memberRepository).save(any(ProjectMember.class));
    }

    @Test
    void getProject_returnsProject_whenRequesterIsMember() {
        ProjectId id = new ProjectId(UUID.randomUUID());
        given(memberRepository.findByProjectAndUser(id, viewer)).willReturn(Optional.of(viewerMember(id)));
        given(projectRepository.findById(id)).willReturn(Optional.of(project(id, owner)));

        Project result = service.getProject(id, viewer);

        assertThat(result).isNotNull();
    }

    @Test
    void getProject_throwsAccessDenied_whenRequesterIsNotMember() {
        ProjectId id = new ProjectId(UUID.randomUUID());
        given(memberRepository.findByProjectAndUser(id, stranger)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.getProject(id, stranger))
            .isInstanceOf(ProjectAccessDeniedException.class);
    }

    @Test
    void updateProject_succeeds_forEditor() {
        ProjectId id = new ProjectId(UUID.randomUUID());
        Project existing = project(id, owner);
        given(memberRepository.findByProjectAndUser(id, editor)).willReturn(Optional.of(editorMember(id)));
        given(projectRepository.findById(id)).willReturn(Optional.of(existing));
        given(projectRepository.save(any())).willReturn(existing.withDetails("New", "New Desc"));

        Project result = service.updateProject(id, editor, new UpdateProjectCommand("New", "New Desc"));

        assertThat(result.name()).isEqualTo("New");
    }

    @Test
    void updateProject_throwsAccessDenied_forViewer() {
        ProjectId id = new ProjectId(UUID.randomUUID());
        given(memberRepository.findByProjectAndUser(id, viewer)).willReturn(Optional.of(viewerMember(id)));

        assertThatThrownBy(() -> service.updateProject(id, viewer, new UpdateProjectCommand("X", null)))
            .isInstanceOf(ProjectAccessDeniedException.class);
    }

    @Test
    void deleteProject_succeeds_forOwner() {
        ProjectId id = new ProjectId(UUID.randomUUID());
        given(memberRepository.findByProjectAndUser(id, owner)).willReturn(Optional.of(ownerMember(id)));

        service.deleteProject(id, owner);

        verify(projectRepository).deleteById(id);
    }

    @Test
    void deleteProject_throwsAccessDenied_forEditor() {
        ProjectId id = new ProjectId(UUID.randomUUID());
        given(memberRepository.findByProjectAndUser(id, editor)).willReturn(Optional.of(editorMember(id)));

        assertThatThrownBy(() -> service.deleteProject(id, editor))
            .isInstanceOf(ProjectAccessDeniedException.class);
    }

    @Test
    void addMember_throwsNotFound_whenProjectMissing() {
        ProjectId id = new ProjectId(UUID.randomUUID());
        given(memberRepository.findByProjectAndUser(id, owner)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.addMember(id, owner, new AddMemberCommand(viewer, ProjectRole.VIEWER)))
            .isInstanceOf(ProjectAccessDeniedException.class);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Project project(ProjectId id, UserId ownerId) {
        Instant now = Instant.now();
        return new Project(id, "Test Project", "Desc", ProjectStatus.ACTIVE, ownerId, now, now);
    }

    private ProjectMember ownerMember(ProjectId id) {
        return new ProjectMember(id, owner, ProjectRole.OWNER, Instant.now());
    }

    private ProjectMember editorMember(ProjectId id) {
        return new ProjectMember(id, editor, ProjectRole.EDITOR, Instant.now());
    }

    private ProjectMember viewerMember(ProjectId id) {
        return new ProjectMember(id, viewer, ProjectRole.VIEWER, Instant.now());
    }
}
