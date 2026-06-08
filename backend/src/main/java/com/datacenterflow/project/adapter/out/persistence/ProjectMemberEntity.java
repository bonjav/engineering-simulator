package com.datacenterflow.project.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "project_members",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_project_members_project_user",
        columnNames = {"project_id", "user_id"}
    )
)
class ProjectMemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", columnDefinition = "uuid", nullable = false)
    private UUID projectId;

    @Column(name = "user_id", columnDefinition = "uuid", nullable = false)
    private UUID userId;

    @Column(name = "role", nullable = false, length = 20)
    private String role;

    @CreationTimestamp
    @Column(name = "added_at", nullable = false, updatable = false)
    private Instant addedAt;

    protected ProjectMemberEntity() {}

    ProjectMemberEntity(UUID projectId, UUID userId, String role) {
        this.projectId = projectId;
        this.userId = userId;
        this.role = role;
    }

    Long getId() { return id; }
    UUID getProjectId() { return projectId; }
    UUID getUserId() { return userId; }
    String getRole() { return role; }
    Instant getAddedAt() { return addedAt; }

    void setRole(String role) { this.role = role; }
}
