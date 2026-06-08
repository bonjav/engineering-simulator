package com.datacenterflow.cad.adapter.in.web;

import com.datacenterflow.auth.domain.model.UserId;
import com.datacenterflow.cad.domain.exception.CadFileNotFoundException;
import com.datacenterflow.cad.domain.exception.InvalidCadFileException;
import com.datacenterflow.cad.domain.model.CadFile;
import com.datacenterflow.cad.domain.model.CadFileId;
import com.datacenterflow.cad.domain.model.CadFileStatus;
import com.datacenterflow.cad.domain.port.in.DeleteCadFileUseCase;
import com.datacenterflow.cad.domain.port.in.GetCadFileUseCase;
import com.datacenterflow.cad.domain.port.in.GetDownloadUrlUseCase;
import com.datacenterflow.cad.domain.port.in.ListCadFilesUseCase;
import com.datacenterflow.cad.domain.port.in.UploadCadFileUseCase;
import com.datacenterflow.infrastructure.exception.GlobalExceptionHandler;
import com.datacenterflow.infrastructure.security.SecurityConfig;
import com.datacenterflow.infrastructure.security.SupabaseProperties;
import com.datacenterflow.project.domain.model.ProjectId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CadFileController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class CadFileControllerTest {

    @Autowired MockMvc mockMvc;

    @MockBean UploadCadFileUseCase uploadFile;
    @MockBean GetCadFileUseCase getFile;
    @MockBean GetDownloadUrlUseCase getDownloadUrl;
    @MockBean ListCadFilesUseCase listFiles;
    @MockBean DeleteCadFileUseCase deleteFile;
    @MockBean JwtDecoder jwtDecoder;
    @MockBean SupabaseProperties supabaseProperties;

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID PROJECT_ID = UUID.randomUUID();
    private static final UUID FILE_ID = UUID.randomUUID();

    @Test
    void upload_returns201_withFileMetadata() throws Exception {
        given(uploadFile.upload(any(), any(), any())).willReturn(testFile());
        MockMultipartFile multipart = new MockMultipartFile(
            "file", "rack.step", "application/step", new byte[]{1, 2, 3}
        );

        mockMvc.perform(multipart("/api/v1/projects/{projectId}/files", PROJECT_ID)
                .file(multipart)
                .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(FILE_ID.toString()))
            .andExpect(jsonPath("$.originalFilename").value("rack.step"))
            .andExpect(jsonPath("$.status").value("READY"));
    }

    @Test
    void upload_returns422_whenInvalidFileType() throws Exception {
        willThrow(new InvalidCadFileException("Unsupported file type: .pdf"))
            .given(uploadFile).upload(any(), any(), any());

        MockMultipartFile multipart = new MockMultipartFile(
            "file", "document.pdf", "application/pdf", new byte[]{1}
        );

        mockMvc.perform(multipart("/api/v1/projects/{projectId}/files", PROJECT_ID)
                .file(multipart)
                .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
            .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void list_returns200_withFileList() throws Exception {
        given(listFiles.listByProject(any(), any())).willReturn(List.of(testFile()));

        mockMvc.perform(get("/api/v1/projects/{projectId}/files", PROJECT_ID)
                .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(FILE_ID.toString()));
    }

    @Test
    void getFile_returns404_whenNotFound() throws Exception {
        given(getFile.getFile(any(), any(), any()))
            .willThrow(new CadFileNotFoundException(new CadFileId(FILE_ID)));

        mockMvc.perform(get("/api/v1/projects/{projectId}/files/{fileId}", PROJECT_ID, FILE_ID)
                .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
            .andExpect(status().isNotFound());
    }

    @Test
    void downloadUrl_returns200_withSignedUrl() throws Exception {
        given(getDownloadUrl.getDownloadUrl(any(), any(), any()))
            .willReturn("https://storage.supabase.co/signed?token=abc");

        mockMvc.perform(get("/api/v1/projects/{projectId}/files/{fileId}/download-url", PROJECT_ID, FILE_ID)
                .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.url").value("https://storage.supabase.co/signed?token=abc"))
            .andExpect(jsonPath("$.expiresInSeconds").value(3600));
    }

    @Test
    void delete_returns204_onSuccess() throws Exception {
        willDoNothing().given(deleteFile).deleteFile(any(), any(), any());

        mockMvc.perform(delete("/api/v1/projects/{projectId}/files/{fileId}", PROJECT_ID, FILE_ID)
                .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
            .andExpect(status().isNoContent());
    }

    @Test
    void anyEndpoint_returns401_whenNoToken() throws Exception {
        mockMvc.perform(get("/api/v1/projects/{projectId}/files", PROJECT_ID))
            .andExpect(status().isUnauthorized());
    }

    private CadFile testFile() {
        String path = PROJECT_ID + "/" + FILE_ID + "_rack.step";
        return new CadFile(
            new CadFileId(FILE_ID), new ProjectId(PROJECT_ID), new UserId(USER_ID),
            "rack.step", "application/step", 2048L,
            path, CadFileStatus.READY, Instant.now()
        );
    }
}
