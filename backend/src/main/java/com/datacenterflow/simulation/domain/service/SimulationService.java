package com.datacenterflow.simulation.domain.service;

import com.datacenterflow.auth.domain.model.UserId;
import com.datacenterflow.project.domain.exception.ProjectAccessDeniedException;
import com.datacenterflow.project.domain.model.ProjectId;
import com.datacenterflow.simulation.domain.exception.SimulationRunNotFoundException;
import com.datacenterflow.simulation.domain.model.SimulationRun;
import com.datacenterflow.simulation.domain.model.SimulationRunId;
import com.datacenterflow.simulation.domain.model.SimulationStatus;
import com.datacenterflow.simulation.domain.port.in.GetSimulationRunUseCase;
import com.datacenterflow.simulation.domain.port.in.ListSimulationRunsUseCase;
import com.datacenterflow.simulation.domain.port.in.StartSimulationUseCase;
import com.datacenterflow.simulation.domain.port.out.CadFileAccessPort;
import com.datacenterflow.simulation.domain.port.out.ProjectMembershipPort;
import com.datacenterflow.simulation.domain.port.out.SimulationRunRepository;
import com.datacenterflow.simulation.domain.port.out.WorkflowLaunchPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class SimulationService implements
    StartSimulationUseCase,
    GetSimulationRunUseCase,
    ListSimulationRunsUseCase {

    private final SimulationRunRepository runRepository;
    private final ProjectMembershipPort membershipPort;
    private final CadFileAccessPort cadFileAccessPort;
    private final WorkflowLaunchPort workflowLaunchPort;

    public SimulationService(
        SimulationRunRepository runRepository,
        ProjectMembershipPort membershipPort,
        CadFileAccessPort cadFileAccessPort,
        WorkflowLaunchPort workflowLaunchPort
    ) {
        this.runRepository = runRepository;
        this.membershipPort = membershipPort;
        this.cadFileAccessPort = cadFileAccessPort;
        this.workflowLaunchPort = workflowLaunchPort;
    }

    @Override
    public SimulationRun startSimulation(ProjectId projectId, UserId requestedBy, StartSimulationCommand command) {
        requireMember(projectId, requestedBy);

        String storagePath = cadFileAccessPort.getStoragePath(command.cadFileId())
            .orElseThrow(() -> new IllegalArgumentException(
                "CAD file not found: " + command.cadFileId().value()
            ));

        SimulationRunId runId = new SimulationRunId(UUID.randomUUID());
        Instant now = Instant.now();

        // Persist before launching — ensures DB record exists when activities run
        SimulationRun pending = new SimulationRun(
            runId, projectId, command.cadFileId(), requestedBy,
            SimulationStatus.PENDING, null, null, null, now, null
        );
        runRepository.save(pending);

        String workflowId = workflowLaunchPort.launch(runId, projectId, command.cadFileId(), storagePath);

        return runRepository.save(new SimulationRun(
            runId, projectId, command.cadFileId(), requestedBy,
            SimulationStatus.PENDING, workflowId, null, null, now, null
        ));
    }

    @Override
    @Transactional(readOnly = true)
    public SimulationRun getSimulationRun(ProjectId projectId, SimulationRunId runId, UserId requesterId) {
        requireMember(projectId, requesterId);
        return runRepository.findById(runId)
            .orElseThrow(() -> new SimulationRunNotFoundException(runId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SimulationRun> listByProject(ProjectId projectId, UserId requesterId) {
        requireMember(projectId, requesterId);
        return runRepository.findAllByProject(projectId);
    }

    private void requireMember(ProjectId projectId, UserId userId) {
        if (!membershipPort.isMember(projectId, userId)) {
            throw new ProjectAccessDeniedException(userId, projectId);
        }
    }
}
