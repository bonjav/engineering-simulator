package com.datacenterflow.project.adapter.in.web.dto;

import com.datacenterflow.auth.domain.model.UserId;
import com.datacenterflow.project.domain.model.ProjectRole;
import com.datacenterflow.project.domain.port.in.AddProjectMemberUseCase.AddMemberCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Request body for adding a project member")
public record AddMemberRequest(

    @NotNull
    @Schema(description = "User UUID to add", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    UUID userId,

    @NotNull
    @Schema(description = "Role to grant", example = "EDITOR", allowableValues = {"VIEWER", "EDITOR"})
    ProjectRole role
) {

    public AddMemberCommand toCommand() {
        return new AddMemberCommand(new UserId(userId), role);
    }
}
