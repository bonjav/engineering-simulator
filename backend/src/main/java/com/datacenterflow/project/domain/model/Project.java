package com.datacenterflow.project.domain.model;

import com.datacenterflow.auth.domain.model.UserId;

import java.time.Instant;

public record Project(
    ProjectId id,
    String name,
    String description,
    ProjectStatus status,
    UserId ownerId,
    Instant createdAt,
    Instant updatedAt
) {

    public Project withDetails(String name, String description) {
        return new Project(id, name, description, status, ownerId, createdAt, Instant.now());
    }

    public Project archive() {
        return new Project(id, name, description, ProjectStatus.ARCHIVED, ownerId, createdAt, Instant.now());
    }
}
