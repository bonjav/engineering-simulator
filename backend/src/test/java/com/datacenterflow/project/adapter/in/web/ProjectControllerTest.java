package com.datacenterflow.project.adapter.in.web;

import com.datacenterflow.auth.domain.model.UserId;
import com.datacenterflow.infrastructure.exception.GlobalExceptionHandler;
import com.datacenterflow.infrastructure.security.SecurityConfig;
import com.datacenterflow.infrastructure.security.SupabaseProperties;
import com.datacenterflow.project.domain.exception.ProjectAccessDeniedException;
import com.datacenterflow.project.domain.exception.ProjectNotFoundException;
import com.datacenterflow.project.domain.model.Project;
import com.datacenterflow.project.domain.model.ProjectId;
import com.datacenterflow.project.domain.model.ProjectStatus;
import com.datacenterflow.project.domain.port.in.CreateProjectUseCase;
import com.datacenterflow.project.domain.port.in.DeleteProjectUseCase;
import com.datacenterflow.project.domain.port.in.GetProjectUseCase;
import com.datacenterflow.project.domain.port.in.ListProjectsUseCase;
import com.datacenterflow.project.domain.port.in.UpdateProjectUseCase;
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
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class ProjectControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean CreateProjectUseCase createProject;
    @MockBean GetProjectUseCase getProject;
    @MockBean ListProjectsUseCase listProjects;
    @MockBean UpdateProjectUseCase updateProject;
    @MockBean DeleteProjectUseCase deleteProject;
    @MockBean JwtDecoder jwtDecoder;
    @MockBean SupabaseProperties supabaseProperties;

    private static final UUID OWNER_ID = UUID.randomUUID();
    private static final UUID PROJECT_ID = UUID.randomUUID();

    @Test
    void createProject_returns201_withCreatedProject() throws Exception {
        given(createProject.createProject(any(), any())).willReturn(testProject());
        String body = objectMapper.writeValueAsString(Map.of("name", "Rack Layout Q3", "description", "Test"));

        mockMvc.perform(post("/api/v1/projects")
                .with(jwt().jwt(j -> j.subject(OWNER_ID.toString())))
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(PROJECT_ID.toString()))
            .andExpect(jsonPath("$.name").value("Rack Layout Q3"))
            .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void createProject_returns400_whenNameIsBlank() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("name", ""));

        mockMvc.perform(post("/api/v1/projects")
                .with(jwt().jwt(j -> j.subject(OWNER_ID.toString())))
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest());
    }

    @Test
    void listProjects_returns200_withProjectList() throws Exception {
        given(listProjects.listAccessibleProjects(any())).willReturn(List.of(testProject()));

        mockMvc.perform(get("/api/v1/projects")
                .with(jwt().jwt(j -> j.subject(OWNER_ID.toString()))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(PROJECT_ID.toString()));
    }

    @Test
    void getProject_returns404_whenProjectNotFound() throws Exception {
        given(getProject.getProject(any(), any()))
            .willThrow(new ProjectNotFoundException(new ProjectId(PROJECT_ID)));

        mockMvc.perform(get("/api/v1/projects/" + PROJECT_ID)
                .with(jwt().jwt(j -> j.subject(OWNER_ID.toString()))))
            .andExpect(status().isNotFound());
    }

    @Test
    void getProject_returns403_whenAccessDenied() throws Exception {
        UUID stranger = UUID.randomUUID();
        given(getProject.getProject(any(), any()))
            .willThrow(new ProjectAccessDeniedException(new UserId(stranger), new ProjectId(PROJECT_ID)));

        mockMvc.perform(get("/api/v1/projects/" + PROJECT_ID)
                .with(jwt().jwt(j -> j.subject(stranger.toString()))))
            .andExpect(status().isForbidden());
    }

    @Test
    void deleteProject_returns204_onSuccess() throws Exception {
        willDoNothing().given(deleteProject).deleteProject(any(), any());

        mockMvc.perform(delete("/api/v1/projects/" + PROJECT_ID)
                .with(jwt().jwt(j -> j.subject(OWNER_ID.toString()))))
            .andExpect(status().isNoContent());
    }

    @Test
    void deleteProject_returns403_forNonOwner() throws Exception {
        willThrow(new ProjectAccessDeniedException(new UserId(OWNER_ID), new ProjectId(PROJECT_ID)))
            .given(deleteProject).deleteProject(any(), any());

        mockMvc.perform(delete("/api/v1/projects/" + PROJECT_ID)
                .with(jwt().jwt(j -> j.subject(OWNER_ID.toString()))))
            .andExpect(status().isForbidden());
    }

    @Test
    void anyEndpoint_returns401_whenNoToken() throws Exception {
        mockMvc.perform(get("/api/v1/projects"))
            .andExpect(status().isUnauthorized());
    }

    private Project testProject() {
        Instant now = Instant.now();
        return new Project(
            new ProjectId(PROJECT_ID), "Rack Layout Q3", "Test",
            ProjectStatus.ACTIVE, new UserId(OWNER_ID), now, now
        );
    }
}
