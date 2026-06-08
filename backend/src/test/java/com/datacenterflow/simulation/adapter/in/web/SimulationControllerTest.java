package com.datacenterflow.simulation.adapter.in.web;

import com.datacenterflow.auth.domain.model.UserId;
import com.datacenterflow.cad.domain.model.CadFileId;
import com.datacenterflow.infrastructure.exception.GlobalExceptionHandler;
import com.datacenterflow.infrastructure.security.SecurityConfig;
import com.datacenterflow.infrastructure.security.SupabaseProperties;
import com.datacenterflow.project.domain.model.ProjectId;
import com.datacenterflow.simulation.domain.exception.SimulationRunNotFoundException;
import com.datacenterflow.simulation.domain.model.SimulationRun;
import com.datacenterflow.simulation.domain.model.SimulationRunId;
import com.datacenterflow.simulation.domain.model.SimulationStatus;
import com.datacenterflow.simulation.domain.port.in.GetSimulationRunUseCase;
import com.datacenterflow.simulation.domain.port.in.ListSimulationRunsUseCase;
import com.datacenterflow.simulation.domain.port.in.StartSimulationUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SimulationController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class SimulationControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean StartSimulationUseCase startSimulation;
    @MockBean GetSimulationRunUseCase getSimulationRun;
    @MockBean ListSimulationRunsUseCase listSimulationRuns;
    @MockBean JwtDecoder jwtDecoder;
    @MockBean SupabaseProperties supabaseProperties;

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID PROJECT_ID = UUID.randomUUID();
    private static final UUID CAD_FILE_ID = UUID.randomUUID();
    private static final UUID RUN_ID = UUID.randomUUID();

    @Test
    void start_returns202_withPendingRun() throws Exception {
        given(startSimulation.startSimulation(any(), any(), any())).willReturn(pendingRun());
        String body = objectMapper.writeValueAsString(Map.of("cadFileId", CAD_FILE_ID));

        mockMvc.perform(post("/api/v1/projects/{projectId}/simulations", PROJECT_ID)
                .with(jwt().jwt(j -> j.subject(USER_ID.toString())))
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.id").value(RUN_ID.toString()))
            .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void start_returns400_whenCadFileIdMissing() throws Exception {
        mockMvc.perform(post("/api/v1/projects/{projectId}/simulations", PROJECT_ID)
                .with(jwt().jwt(j -> j.subject(USER_ID.toString())))
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void get_returns200_withRunDetails() throws Exception {
        given(getSimulationRun.getSimulationRun(any(), any(), any())).willReturn(pendingRun());

        mockMvc.perform(get("/api/v1/projects/{projectId}/simulations/{runId}", PROJECT_ID, RUN_ID)
                .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.temporalWorkflowId").value("simulation-wf-123"));
    }

    @Test
    void get_returns404_whenRunNotFound() throws Exception {
        given(getSimulationRun.getSimulationRun(any(), any(), any()))
            .willThrow(new SimulationRunNotFoundException(new SimulationRunId(RUN_ID)));

        mockMvc.perform(get("/api/v1/projects/{projectId}/simulations/{runId}", PROJECT_ID, RUN_ID)
                .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
            .andExpect(status().isNotFound());
    }

    @Test
    void list_returns200_withRunList() throws Exception {
        given(listSimulationRuns.listByProject(any(), any())).willReturn(List.of(pendingRun()));

        mockMvc.perform(get("/api/v1/projects/{projectId}/simulations", PROJECT_ID)
                .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void anyEndpoint_returns401_whenNoToken() throws Exception {
        mockMvc.perform(get("/api/v1/projects/{projectId}/simulations", PROJECT_ID))
            .andExpect(status().isUnauthorized());
    }

    private SimulationRun pendingRun() {
        return new SimulationRun(
            new SimulationRunId(RUN_ID),
            new ProjectId(PROJECT_ID),
            new CadFileId(CAD_FILE_ID),
            new UserId(USER_ID),
            SimulationStatus.PENDING,
            "simulation-wf-123",
            null, null,
            Instant.now(), null
        );
    }
}
