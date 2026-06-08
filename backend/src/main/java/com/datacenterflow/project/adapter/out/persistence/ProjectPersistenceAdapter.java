package com.datacenterflow.project.adapter.out.persistence;

import com.datacenterflow.auth.domain.model.UserId;
import com.datacenterflow.project.domain.model.Project;
import com.datacenterflow.project.domain.model.ProjectId;
import com.datacenterflow.project.domain.model.ProjectMember;
import com.datacenterflow.project.domain.model.ProjectRole;
import com.datacenterflow.project.domain.model.ProjectStatus;
import com.datacenterflow.project.domain.port.out.ProjectMemberRepository;
import com.datacenterflow.project.domain.port.out.ProjectRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
class ProjectPersistenceAdapter implements ProjectRepository, ProjectMemberRepository {

    private final ProjectJpaRepository projectJpa;
    private final ProjectMemberJpaRepository memberJpa;

    ProjectPersistenceAdapter(ProjectJpaRepository projectJpa, ProjectMemberJpaRepository memberJpa) {
        this.projectJpa = projectJpa;
        this.memberJpa = memberJpa;
    }

    // ── ProjectRepository ─────────────────────────────────────────────────────

    @Override
    public Optional<Project> findById(ProjectId id) {
        return projectJpa.findById(id.value()).map(this::toDomain);
    }

    @Override
    public List<Project> findAllAccessibleByUser(UserId userId) {
        return projectJpa.findAllAccessibleByUser(userId.value()).stream()
            .map(this::toDomain)
            .toList();
    }

    @Override
    public Project save(Project project) {
        ProjectEntity entity = projectJpa.findById(project.id().value())
            .orElseGet(() -> new ProjectEntity(
                project.id().value(),
                project.name(),
                project.description(),
                project.status().name(),
                project.ownerId().value()
            ));
        entity.setName(project.name());
        entity.setDescription(project.description());
        entity.setStatus(project.status().name());
        return toDomain(projectJpa.save(entity));
    }

    @Override
    public void deleteById(ProjectId id) {
        projectJpa.deleteById(id.value());
    }

    // ── ProjectMemberRepository ───────────────────────────────────────────────

    @Override
    public Optional<ProjectMember> findByProjectAndUser(ProjectId projectId, UserId userId) {
        return memberJpa.findByProjectIdAndUserId(projectId.value(), userId.value())
            .map(this::toDomain);
    }

    @Override
    public List<ProjectMember> findAllByProject(ProjectId projectId) {
        return memberJpa.findAllByProjectId(projectId.value()).stream()
            .map(this::toDomain)
            .toList();
    }

    @Override
    public ProjectMember save(ProjectMember member) {
        ProjectMemberEntity entity = memberJpa
            .findByProjectIdAndUserId(member.projectId().value(), member.userId().value())
            .orElseGet(() -> new ProjectMemberEntity(
                member.projectId().value(),
                member.userId().value(),
                member.role().name()
            ));
        entity.setRole(member.role().name());
        return toDomain(memberJpa.save(entity));
    }

    @Override
    public void delete(ProjectId projectId, UserId userId) {
        memberJpa.deleteByProjectIdAndUserId(projectId.value(), userId.value());
    }

    // ── Mappers ───────────────────────────────────────────────────────────────

    private Project toDomain(ProjectEntity e) {
        return new Project(
            new ProjectId(e.getId()),
            e.getName(),
            e.getDescription(),
            ProjectStatus.valueOf(e.getStatus()),
            new UserId(e.getOwnerId()),
            e.getCreatedAt(),
            e.getUpdatedAt()
        );
    }

    private ProjectMember toDomain(ProjectMemberEntity e) {
        return new ProjectMember(
            new ProjectId(e.getProjectId()),
            new UserId(e.getUserId()),
            ProjectRole.valueOf(e.getRole()),
            e.getAddedAt()
        );
    }
}
