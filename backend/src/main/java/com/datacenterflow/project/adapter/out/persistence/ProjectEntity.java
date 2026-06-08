package com.datacenterflow.project.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "projects")
class ProjectEntity {

    @Id
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "owner_id", columnDefinition = "uuid", nullable = false, updatable = false)
    private UUID ownerId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ProjectEntity() {}

    ProjectEntity(UUID id, String name, String description, String status, UUID ownerId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
        this.ownerId = ownerId;
    }

    UUID getId() { return id; }
    String getName() { return name; }
    String getDescription() { return description; }
    String getStatus() { return status; }
    UUID getOwnerId() { return ownerId; }
    Instant getCreatedAt() { return createdAt; }
    Instant getUpdatedAt() { return updatedAt; }

    void setName(String name) { this.name = name; }
    void setDescription(String description) { this.description = description; }
    void setStatus(String status) { this.status = status; }
}
