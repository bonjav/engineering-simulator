package com.datacenterflow.simulation.adapter.out.persistence;

import com.datacenterflow.auth.domain.model.UserId;
import com.datacenterflow.cad.domain.model.CadFileId;
import com.datacenterflow.project.domain.model.ProjectId;
import com.datacenterflow.simulation.domain.model.SimulationReport;
import com.datacenterflow.simulation.domain.model.SimulationRun;
import com.datacenterflow.simulation.domain.model.SimulationRunId;
import com.datacenterflow.simulation.domain.model.SimulationStatus;
import com.datacenterflow.simulation.domain.port.out.SimulationRunRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
class SimulationRunPersistenceAdapter implements SimulationRunRepository {

    private final SimulationRunJpaRepository jpaRepository;
    private final ObjectMapper objectMapper;

    SimulationRunPersistenceAdapter(SimulationRunJpaRepository jpaRepository, ObjectMapper objectMapper) {
        this.jpaRepository = jpaRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<SimulationRun> findById(SimulationRunId id) {
        return jpaRepository.findById(id.value()).map(this::toDomain);
    }

    @Override
    public List<SimulationRun> findAllByProject(ProjectId projectId) {
        return jpaRepository.findAllByProjectIdOrderByStartedAtDesc(projectId.value()).stream()
            .map(this::toDomain)
            .toList();
    }

    @Override
    public SimulationRun save(SimulationRun run) {
        SimulationRunEntity entity = jpaRepository.findById(run.id().value())
            .orElseGet(() -> new SimulationRunEntity(
                run.id().value(), run.projectId().value(), run.cadFileId().value(),
                run.requestedBy().value(), run.status().name(), run.temporalWorkflowId()
            ));

        entity.setStatus(run.status().name());
        entity.setTemporalWorkflowId(run.temporalWorkflowId());
        entity.setReportJson(run.report() != null ? toJson(run.report()) : null);
        entity.setErrorMessage(run.errorMessage());
        entity.setCompletedAt(run.completedAt());

        return toDomain(jpaRepository.save(entity));
    }

    // ── Mappers ───────────────────────────────────────────────────────────────

    private SimulationRun toDomain(SimulationRunEntity e) {
        return new SimulationRun(
            new SimulationRunId(e.getId()),
            new ProjectId(e.getProjectId()),
            new CadFileId(e.getCadFileId()),
            new UserId(e.getRequestedBy()),
            SimulationStatus.valueOf(e.getStatus()),
            e.getTemporalWorkflowId(),
            e.getReportJson() != null ? fromJson(e.getReportJson()) : null,
            e.getErrorMessage(),
            e.getStartedAt(),
            e.getCompletedAt()
        );
    }

    private String toJson(SimulationReport report) {
        try {
            return objectMapper.writeValueAsString(report);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialise SimulationReport", e);
        }
    }

    private SimulationReport fromJson(String json) {
        try {
            return objectMapper.readValue(json, SimulationReport.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialise SimulationReport", e);
        }
    }
}
