package com.datacenterflow.simulation.adapter.in.web.dto;

import com.datacenterflow.simulation.domain.model.SimulationReport;
import com.datacenterflow.simulation.domain.model.SimulationRun;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Simulation run status and results")
public record SimulationRunResponse(

    @Schema(description = "Run UUID") String id,
    @Schema(description = "Project UUID") String projectId,
    @Schema(description = "CAD file UUID") String cadFileId,
    @Schema(description = "Requesting user UUID") String requestedBy,
    @Schema(description = "Workflow status", example = "RUNNING") String status,
    @Schema(description = "Temporal workflow ID") String temporalWorkflowId,
    @Schema(description = "Simulation report — present only when status is COMPLETED") SimulationReport report,
    @Schema(description = "Error details — present only when status is FAILED") String errorMessage,
    @Schema(description = "Run start timestamp") Instant startedAt,
    @Schema(description = "Completion timestamp") Instant completedAt
) {

    public static SimulationRunResponse from(SimulationRun run) {
        return new SimulationRunResponse(
            run.id().value().toString(),
            run.projectId().value().toString(),
            run.cadFileId().value().toString(),
            run.requestedBy().value().toString(),
            run.status().name(),
            run.temporalWorkflowId(),
            run.report(),
            run.errorMessage(),
            run.startedAt(),
            run.completedAt()
        );
    }
}
