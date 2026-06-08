package com.datacenterflow.project.adapter.in.web.dto;

import com.datacenterflow.project.domain.port.in.CreateProjectUseCase.CreateProjectCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request body for creating a project")
public record CreateProjectRequest(

    @NotBlank
    @Size(max = 100)
    @Schema(description = "Project name", example = "Rack Layout Q3")
    String name,

    @Size(max = 500)
    @Schema(description = "Optional project description", example = "Airflow analysis for Q3 expansion")
    String description
) {

    public CreateProjectCommand toCommand() {
        return new CreateProjectCommand(name, description);
    }
}
