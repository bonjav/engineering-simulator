package com.datacenterflow.simulation.domain.model;

import com.datacenterflow.auth.domain.model.UserId;
import com.datacenterflow.cad.domain.model.CadFileId;
import com.datacenterflow.project.domain.model.ProjectId;

import java.time.Instant;

public record SimulationRun(
    SimulationRunId id,
    ProjectId projectId,
    CadFileId cadFileId,
    UserId requestedBy,
    SimulationStatus status,
    String temporalWorkflowId,
    SimulationReport report,      // null until COMPLETED
    String errorMessage,          // null unless FAILED
    Instant startedAt,
    Instant completedAt           // null until COMPLETED or FAILED
) {

    public SimulationRun withStatus(SimulationStatus newStatus) {
        boolean terminal = newStatus == SimulationStatus.COMPLETED || newStatus == SimulationStatus.FAILED;
        return new SimulationRun(id, projectId, cadFileId, requestedBy, newStatus,
            temporalWorkflowId, report, errorMessage, startedAt,
            terminal ? Instant.now() : completedAt);
    }

    public SimulationRun withReport(SimulationReport report) {
        return new SimulationRun(id, projectId, cadFileId, requestedBy, SimulationStatus.COMPLETED,
            temporalWorkflowId, report, null, startedAt, Instant.now());
    }

    public SimulationRun withError(String errorMessage) {
        return new SimulationRun(id, projectId, cadFileId, requestedBy, SimulationStatus.FAILED,
            temporalWorkflowId, null, errorMessage, startedAt, Instant.now());
    }
}
