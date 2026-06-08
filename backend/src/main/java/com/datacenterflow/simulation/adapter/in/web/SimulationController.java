package com.datacenterflow.simulation.adapter.in.web;

import com.datacenterflow.auth.domain.model.UserId;
import com.datacenterflow.project.domain.model.ProjectId;
import com.datacenterflow.simulation.adapter.in.web.dto.SimulationRunResponse;
import com.datacenterflow.simulation.adapter.in.web.dto.StartSimulationRequest;
import com.datacenterflow.simulation.domain.model.SimulationRunId;
import com.datacenterflow.simulation.domain.port.in.GetSimulationRunUseCase;
import com.datacenterflow.simulation.domain.port.in.ListSimulationRunsUseCase;
import com.datacenterflow.simulation.domain.port.in.StartSimulationUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/simulations")
@Tag(name = "Simulations", description = "Trigger and monitor CAD simulation runs")
@SecurityRequirement(name = "bearerAuth")
public class SimulationController {

    private final StartSimulationUseCase startSimulation;
    private final GetSimulationRunUseCase getSimulationRun;
    private final ListSimulationRunsUseCase listSimulationRuns;

    public SimulationController(
        StartSimulationUseCase startSimulation,
        GetSimulationRunUseCase getSimulationRun,
        ListSimulationRunsUseCase listSimulationRuns
    ) {
        this.startSimulation = startSimulation;
        this.getSimulationRun = getSimulationRun;
        this.listSimulationRuns = listSimulationRuns;
    }

    @PostMapping
    @Operation(
        summary = "Start a simulation run",
        description = "Enqueues a Temporal workflow: CAD parse → simulation → report generation."
    )
    public ResponseEntity<SimulationRunResponse> start(
        JwtAuthenticationToken auth,
        @PathVariable UUID projectId,
        @Valid @RequestBody StartSimulationRequest request
    ) {
        UserId requestedBy = UserId.of(auth.getName());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(SimulationRunResponse.from(
            startSimulation.startSimulation(new ProjectId(projectId), requestedBy, request.toCommand())
        ));
    }

    @GetMapping
    @Operation(summary = "List all simulation runs for a project")
    public ResponseEntity<List<SimulationRunResponse>> list(
        JwtAuthenticationToken auth,
        @PathVariable UUID projectId
    ) {
        UserId requesterId = UserId.of(auth.getName());
        return ResponseEntity.ok(
            listSimulationRuns.listByProject(new ProjectId(projectId), requesterId).stream()
                .map(SimulationRunResponse::from)
                .toList()
        );
    }

    @GetMapping("/{runId}")
    @Operation(
        summary = "Get simulation run status and report",
        description = "Poll this endpoint to track progress. Report is populated once status reaches COMPLETED."
    )
    public ResponseEntity<SimulationRunResponse> get(
        JwtAuthenticationToken auth,
        @PathVariable UUID projectId,
        @PathVariable UUID runId
    ) {
        UserId requesterId = UserId.of(auth.getName());
        return ResponseEntity.ok(SimulationRunResponse.from(
            getSimulationRun.getSimulationRun(
                new ProjectId(projectId), new SimulationRunId(runId), requesterId
            )
        ));
    }
}
