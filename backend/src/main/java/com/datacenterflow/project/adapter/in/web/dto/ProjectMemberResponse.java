package com.datacenterflow.project.adapter.in.web.dto;

import com.datacenterflow.project.domain.model.ProjectMember;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Project member details")
public record ProjectMemberResponse(

    @Schema(description = "User UUID") String userId,
    @Schema(description = "Role in the project", example = "EDITOR") String role,
    @Schema(description = "When the member was added") Instant addedAt
) {

    public static ProjectMemberResponse from(ProjectMember m) {
        return new ProjectMemberResponse(
            m.userId().value().toString(),
            m.role().name(),
            m.addedAt()
        );
    }
}
