package com.datacenterflow.cad.adapter.in.web;

import com.datacenterflow.auth.domain.model.UserId;
import com.datacenterflow.cad.adapter.in.web.dto.CadFileResponse;
import com.datacenterflow.cad.adapter.in.web.dto.DownloadUrlResponse;
import com.datacenterflow.cad.domain.model.CadFileId;
import com.datacenterflow.cad.domain.port.in.DeleteCadFileUseCase;
import com.datacenterflow.cad.domain.port.in.GetCadFileUseCase;
import com.datacenterflow.cad.domain.port.in.GetDownloadUrlUseCase;
import com.datacenterflow.cad.domain.port.in.ListCadFilesUseCase;
import com.datacenterflow.cad.domain.port.in.UploadCadFileUseCase;
import com.datacenterflow.cad.domain.port.in.UploadCadFileUseCase.UploadCommand;
import com.datacenterflow.project.domain.model.ProjectId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/files")
@Tag(name = "CAD Files", description = "Upload and manage CAD files for a project")
@SecurityRequirement(name = "bearerAuth")
public class CadFileController {

    private final UploadCadFileUseCase uploadFile;
    private final GetCadFileUseCase getFile;
    private final GetDownloadUrlUseCase getDownloadUrl;
    private final ListCadFilesUseCase listFiles;
    private final DeleteCadFileUseCase deleteFile;

    public CadFileController(
        UploadCadFileUseCase uploadFile,
        GetCadFileUseCase getFile,
        GetDownloadUrlUseCase getDownloadUrl,
        ListCadFilesUseCase listFiles,
        DeleteCadFileUseCase deleteFile
    ) {
        this.uploadFile = uploadFile;
        this.getFile = getFile;
        this.getDownloadUrl = getDownloadUrl;
        this.listFiles = listFiles;
        this.deleteFile = deleteFile;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a CAD file to a project")
    public ResponseEntity<CadFileResponse> upload(
        JwtAuthenticationToken auth,
        @PathVariable UUID projectId,
        @RequestParam("file") MultipartFile file
    ) throws IOException {
        UserId uploadedBy = UserId.of(auth.getName());
        UploadCommand command = new UploadCommand(
            file.getOriginalFilename(),
            file.getContentType(),
            file.getSize(),
            file.getBytes()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(CadFileResponse.from(
            uploadFile.upload(new ProjectId(projectId), uploadedBy, command)
        ));
    }

    @GetMapping
    @Operation(summary = "List all CAD files for a project")
    public ResponseEntity<List<CadFileResponse>> list(
        JwtAuthenticationToken auth,
        @PathVariable UUID projectId
    ) {
        UserId requesterId = UserId.of(auth.getName());
        return ResponseEntity.ok(
            listFiles.listByProject(new ProjectId(projectId), requesterId).stream()
                .map(CadFileResponse::from)
                .toList()
        );
    }

    @GetMapping("/{fileId}")
    @Operation(summary = "Get CAD file metadata")
    public ResponseEntity<CadFileResponse> get(
        JwtAuthenticationToken auth,
        @PathVariable UUID projectId,
        @PathVariable UUID fileId
    ) {
        UserId requesterId = UserId.of(auth.getName());
        return ResponseEntity.ok(CadFileResponse.from(
            getFile.getFile(new ProjectId(projectId), new CadFileId(fileId), requesterId)
        ));
    }

    @GetMapping("/{fileId}/download-url")
    @Operation(summary = "Get a signed download URL (valid 1 hour)")
    public ResponseEntity<DownloadUrlResponse> downloadUrl(
        JwtAuthenticationToken auth,
        @PathVariable UUID projectId,
        @PathVariable UUID fileId
    ) {
        UserId requesterId = UserId.of(auth.getName());
        String url = getDownloadUrl.getDownloadUrl(
            new ProjectId(projectId), new CadFileId(fileId), requesterId
        );
        return ResponseEntity.ok(DownloadUrlResponse.of(url));
    }

    @DeleteMapping("/{fileId}")
    @Operation(summary = "Delete a CAD file")
    public ResponseEntity<Void> delete(
        JwtAuthenticationToken auth,
        @PathVariable UUID projectId,
        @PathVariable UUID fileId
    ) {
        UserId requesterId = UserId.of(auth.getName());
        deleteFile.deleteFile(new ProjectId(projectId), new CadFileId(fileId), requesterId);
        return ResponseEntity.noContent().build();
    }
}
