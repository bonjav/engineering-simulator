package com.datacenterflow.project.adapter.in.web.dto;

import com.datacenterflow.project.domain.model.Project;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Full project representation")
public record ProjectResponse(

    @Schema(description = "Project UUID") String id,
    @Schema(description = "Project name") String name,
    @Schema(description = "Project description") String description,
    @Schema(description = "Project status", example = "ACTIVE") String status,
    @Schema(description = "Owner user UUID") String ownerId,
    @Schema(description = "Creation timestamp") Instant createdAt,
    @Schema(description = "Last-updated timestamp") Instant updatedAt
) {

    public static ProjectResponse from(Project p) {
        return new ProjectResponse(
            p.id().value().toString(),
            p.name(),
            p.description(),
            p.status().name(),
            p.ownerId().value().toString(),
            p.createdAt(),
            p.updatedAt()
        );
    }
}
