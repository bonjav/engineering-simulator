package com.datacenterflow.project.domain.model;

import java.util.Objects;
import java.util.UUID;

public record ProjectId(UUID value) {

    public ProjectId {
        Objects.requireNonNull(value, "ProjectId cannot be null");
    }

    public static ProjectId of(String value) {
        return new ProjectId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
