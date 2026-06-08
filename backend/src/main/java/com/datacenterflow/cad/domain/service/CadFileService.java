package com.datacenterflow.cad.domain.service;

import com.datacenterflow.auth.domain.model.UserId;
import com.datacenterflow.cad.domain.exception.CadFileNotFoundException;
import com.datacenterflow.cad.domain.exception.CadFileStorageException;
import com.datacenterflow.cad.domain.exception.InvalidCadFileException;
import com.datacenterflow.cad.domain.model.CadFile;
import com.datacenterflow.cad.domain.model.CadFileId;
import com.datacenterflow.cad.domain.model.CadFileStatus;
import com.datacenterflow.cad.domain.port.in.DeleteCadFileUseCase;
import com.datacenterflow.cad.domain.port.in.GetCadFileUseCase;
import com.datacenterflow.cad.domain.port.in.GetDownloadUrlUseCase;
import com.datacenterflow.cad.domain.port.in.ListCadFilesUseCase;
import com.datacenterflow.cad.domain.port.in.UploadCadFileUseCase;
import com.datacenterflow.cad.domain.port.out.CadFileRepository;
import com.datacenterflow.cad.domain.port.out.ProjectAccessPort;
import com.datacenterflow.cad.domain.port.out.StoragePort;
import com.datacenterflow.project.domain.exception.ProjectAccessDeniedException;
import com.datacenterflow.project.domain.model.ProjectId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class CadFileService implements
    UploadCadFileUseCase,
    GetCadFileUseCase,
    GetDownloadUrlUseCase,
    ListCadFilesUseCase,
    DeleteCadFileUseCase {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
        "step", "stp", "iges", "igs", "stl", "obj", "3dm", "dwg", "dxf", "brep"
    );

    private static final long MAX_FILE_SIZE_BYTES = 500L * 1024 * 1024; // 500 MB
    private static final Duration SIGNED_URL_TTL = Duration.ofHours(1);

    private final CadFileRepository cadFileRepository;
    private final StoragePort storagePort;
    private final ProjectAccessPort projectAccessPort;

    public CadFileService(
        CadFileRepository cadFileRepository,
        StoragePort storagePort,
        ProjectAccessPort projectAccessPort
    ) {
        this.cadFileRepository = cadFileRepository;
        this.storagePort = storagePort;
        this.projectAccessPort = projectAccessPort;
    }

    @Override
    public CadFile upload(ProjectId projectId, UserId uploadedBy, UploadCommand command) {
        requireMember(projectId, uploadedBy);
        validateFile(command.originalFilename(), command.fileSizeBytes());

        CadFileId fileId = new CadFileId(UUID.randomUUID());
        String storagePath = buildStoragePath(projectId, fileId, command.originalFilename());

        CadFile pending = new CadFile(
            fileId, projectId, uploadedBy,
            command.originalFilename(), command.contentType(),
            command.fileSizeBytes(), storagePath,
            CadFileStatus.UPLOADING, Instant.now()
        );
        cadFileRepository.save(pending);

        try {
            storagePort.upload(storagePath, command.content(), command.contentType());
        } catch (Exception e) {
            cadFileRepository.save(pending.withStatus(CadFileStatus.FAILED));
            throw new CadFileStorageException("Failed to upload file to storage", e);
        }

        return cadFileRepository.save(pending.withStatus(CadFileStatus.READY));
    }

    @Override
    @Transactional(readOnly = true)
    public CadFile getFile(ProjectId projectId, CadFileId fileId, UserId requesterId) {
        requireMember(projectId, requesterId);
        return cadFileRepository.findById(fileId)
            .orElseThrow(() -> new CadFileNotFoundException(fileId));
    }

    @Override
    @Transactional(readOnly = true)
    public String getDownloadUrl(ProjectId projectId, CadFileId fileId, UserId requesterId) {
        requireMember(projectId, requesterId);
        CadFile file = cadFileRepository.findById(fileId)
            .orElseThrow(() -> new CadFileNotFoundException(fileId));
        return storagePort.generateSignedUrl(file.storagePath(), SIGNED_URL_TTL);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CadFile> listByProject(ProjectId projectId, UserId requesterId) {
        requireMember(projectId, requesterId);
        return cadFileRepository.findAllByProject(projectId);
    }

    @Override
    public void deleteFile(ProjectId projectId, CadFileId fileId, UserId requesterId) {
        requireMember(projectId, requesterId);
        CadFile file = cadFileRepository.findById(fileId)
            .orElseThrow(() -> new CadFileNotFoundException(fileId));

        // Only the uploader or a project owner-level check is left to the controller layer.
        // Here we verify at minimum that the requester is a member.
        storagePort.delete(file.storagePath());
        cadFileRepository.deleteById(fileId);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void requireMember(ProjectId projectId, UserId userId) {
        if (!projectAccessPort.isMember(projectId, userId)) {
            throw new ProjectAccessDeniedException(userId, projectId);
        }
    }

    private void validateFile(String filename, long sizeBytes) {
        if (sizeBytes > MAX_FILE_SIZE_BYTES) {
            throw new InvalidCadFileException(
                "File exceeds maximum allowed size of 500 MB"
            );
        }
        String ext = extension(filename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new InvalidCadFileException(
                "Unsupported file type: ." + ext +
                ". Allowed: " + String.join(", ", ALLOWED_EXTENSIONS)
            );
        }
    }

    private String extension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot + 1) : "";
    }

    private String buildStoragePath(ProjectId projectId, CadFileId fileId, String originalFilename) {
        return projectId.value() + "/" + fileId.value() + "_" + sanitize(originalFilename);
    }

    private String sanitize(String filename) {
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
