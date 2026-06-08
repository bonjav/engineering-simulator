package com.datacenterflow.project.adapter.in.web.dto;

import com.datacenterflow.project.domain.port.in.UpdateProjectUseCase.UpdateProjectCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request body for updating a project")
public record UpdateProjectRequest(

    @NotBlank
    @Size(max = 100)
    @Schema(description = "Project name", example = "Rack Layout Q3 — Revised")
    String name,

    @Size(max = 500)
    @Schema(description = "Project description")
    String description
) {

    public UpdateProjectCommand toCommand() {
        return new UpdateProjectCommand(name, description);
    }
}
