package com.datacenterflow.simulation.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "simulation_runs")
class SimulationRunEntity {

    @Id
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "project_id", columnDefinition = "uuid", nullable = false)
    private UUID projectId;

    @Column(name = "cad_file_id", columnDefinition = "uuid", nullable = false)
    private UUID cadFileId;

    @Column(name = "requested_by", columnDefinition = "uuid", nullable = false)
    private UUID requestedBy;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "temporal_workflow_id", length = 255)
    private String temporalWorkflowId;

    @Column(name = "report", columnDefinition = "text")
    private String reportJson;

    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "started_at", nullable = false, updatable = false)
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    protected SimulationRunEntity() {}

    SimulationRunEntity(UUID id, UUID projectId, UUID cadFileId, UUID requestedBy,
                        String status, String temporalWorkflowId) {
        this.id = id;
        this.projectId = projectId;
        this.cadFileId = cadFileId;
        this.requestedBy = requestedBy;
        this.status = status;
        this.temporalWorkflowId = temporalWorkflowId;
    }

    UUID getId() { return id; }
    UUID getProjectId() { return projectId; }
    UUID getCadFileId() { return cadFileId; }
    UUID getRequestedBy() { return requestedBy; }
    String getStatus() { return status; }
    String getTemporalWorkflowId() { return temporalWorkflowId; }
    String getReportJson() { return reportJson; }
    String getErrorMessage() { return errorMessage; }
    Instant getStartedAt() { return startedAt; }
    Instant getCompletedAt() { return completedAt; }

    void setStatus(String status) { this.status = status; }
    void setTemporalWorkflowId(String id) { this.temporalWorkflowId = id; }
    void setReportJson(String json) { this.reportJson = json; }
    void setErrorMessage(String msg) { this.errorMessage = msg; }
    void setCompletedAt(Instant t) { this.completedAt = t; }
}
