package com.datacenterflow.simulation.domain.service;

import com.datacenterflow.auth.domain.model.UserId;
import com.datacenterflow.cad.domain.model.CadFileId;
import com.datacenterflow.project.domain.exception.ProjectAccessDeniedException;
import com.datacenterflow.project.domain.model.ProjectId;
import com.datacenterflow.simulation.domain.exception.SimulationRunNotFoundException;
import com.datacenterflow.simulation.domain.model.SimulationRun;
import com.datacenterflow.simulation.domain.model.SimulationRunId;
import com.datacenterflow.simulation.domain.model.SimulationStatus;
import com.datacenterflow.simulation.domain.port.in.StartSimulationUseCase.StartSimulationCommand;
import com.datacenterflow.simulation.domain.port.out.CadFileAccessPort;
import com.datacenterflow.simulation.domain.port.out.ProjectMembershipPort;
import com.datacenterflow.simulation.domain.port.out.SimulationRunRepository;
import com.datacenterflow.simulation.domain.port.out.WorkflowLaunchPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SimulationServiceTest {

    @Mock SimulationRunRepository runRepository;
    @Mock ProjectMembershipPort membershipPort;
    @Mock CadFileAccessPort cadFileAccessPort;
    @Mock WorkflowLaunchPort workflowLaunchPort;

    private SimulationService service;

    private final ProjectId projectId = new ProjectId(UUID.randomUUID());
    private final UserId userId = new UserId(UUID.randomUUID());
    private final CadFileId cadFileId = new CadFileId(UUID.randomUUID());

    @BeforeEach
    void setUp() {
        service = new SimulationService(runRepository, membershipPort, cadFileAccessPort, workflowLaunchPort);
    }

    @Test
    void startSimulation_createsRunAndLaunchesWorkflow_whenMemberAndFileExists() {
        given(membershipPort.isMember(projectId, userId)).willReturn(true);
        given(cadFileAccessPort.getStoragePath(cadFileId)).willReturn(Optional.of("project/file.step"));
        given(workflowLaunchPort.launch(any(), any(), any(), any())).willReturn("simulation-wf-id");
        given(runRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        SimulationRun run = service.startSimulation(projectId, userId, new StartSimulationCommand(cadFileId));

        assertThat(run.status()).isEqualTo(SimulationStatus.PENDING);
        assertThat(run.projectId()).isEqualTo(projectId);
        verify(workflowLaunchPort).launch(any(), any(), any(), any());
    }

    @Test
    void startSimulation_throwsAccessDenied_whenNotMember() {
        given(membershipPort.isMember(projectId, userId)).willReturn(false);

        assertThatThrownBy(() -> service.startSimulation(projectId, userId, new StartSimulationCommand(cadFileId)))
            .isInstanceOf(ProjectAccessDeniedException.class);
        verify(workflowLaunchPort, never()).launch(any(), any(), any(), any());
    }

    @Test
    void startSimulation_throwsIllegalArgument_whenCadFileNotFound() {
        given(membershipPort.isMember(projectId, userId)).willReturn(true);
        given(cadFileAccessPort.getStoragePath(cadFileId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.startSimulation(projectId, userId, new StartSimulationCommand(cadFileId)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("CAD file not found");
    }

    @Test
    void getSimulationRun_returnsRun_whenMemberAndRunExists() {
        SimulationRunId runId = new SimulationRunId(UUID.randomUUID());
        given(membershipPort.isMember(projectId, userId)).willReturn(true);
        given(runRepository.findById(runId)).willReturn(Optional.of(pendingRun(runId)));

        SimulationRun result = service.getSimulationRun(projectId, runId, userId);

        assertThat(result.id()).isEqualTo(runId);
    }

    @Test
    void getSimulationRun_throwsNotFound_whenRunMissing() {
        SimulationRunId runId = new SimulationRunId(UUID.randomUUID());
        given(membershipPort.isMember(projectId, userId)).willReturn(true);
        given(runRepository.findById(runId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.getSimulationRun(projectId, runId, userId))
            .isInstanceOf(SimulationRunNotFoundException.class);
    }

    @Test
    void listByProject_returnsRuns_whenMember() {
        given(membershipPort.isMember(projectId, userId)).willReturn(true);
        given(runRepository.findAllByProject(projectId)).willReturn(List.of(pendingRun(new SimulationRunId(UUID.randomUUID()))));

        List<SimulationRun> result = service.listByProject(projectId, userId);

        assertThat(result).hasSize(1);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private SimulationRun pendingRun(SimulationRunId id) {
        return new SimulationRun(id, projectId, cadFileId, userId,
            SimulationStatus.PENDING, "simulation-wf-id", null, null, Instant.now(), null);
    }
}
