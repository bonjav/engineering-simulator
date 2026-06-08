package com.datacenterflow.project.domain.model;

import com.datacenterflow.auth.domain.model.UserId;

import java.time.Instant;

public record ProjectMember(
    ProjectId projectId,
    UserId userId,
    ProjectRole role,
    Instant addedAt
) {}
