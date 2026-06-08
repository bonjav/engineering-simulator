package com.datacenterflow.cad.domain.service;

import com.datacenterflow.auth.domain.model.UserId;
import com.datacenterflow.cad.domain.exception.CadFileNotFoundException;
import com.datacenterflow.cad.domain.exception.CadFileStorageException;
import com.datacenterflow.cad.domain.exception.InvalidCadFileException;
import com.datacenterflow.cad.domain.model.CadFile;
import com.datacenterflow.cad.domain.model.CadFileId;
import com.datacenterflow.cad.domain.model.CadFileStatus;
import com.datacenterflow.cad.domain.port.in.UploadCadFileUseCase.UploadCommand;
import com.datacenterflow.cad.domain.port.out.CadFileRepository;
import com.datacenterflow.cad.domain.port.out.ProjectAccessPort;
import com.datacenterflow.cad.domain.port.out.StoragePort;
import com.datacenterflow.project.domain.exception.ProjectAccessDeniedException;
import com.datacenterflow.project.domain.model.ProjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CadFileServiceTest {

    @Mock CadFileRepository cadFileRepository;
    @Mock StoragePort storagePort;
    @Mock ProjectAccessPort projectAccessPort;

    private CadFileService service;

    private final ProjectId projectId = new ProjectId(UUID.randomUUID());
    private final UserId userId = new UserId(UUID.randomUUID());

    @BeforeEach
    void setUp() {
        service = new CadFileService(cadFileRepository, storagePort, projectAccessPort);
    }

    @Test
    void upload_savesMetadataAndUploadsToStorage_whenMemberAndValidFile() {
        given(projectAccessPort.isMember(projectId, userId)).willReturn(true);
        willDoNothing().given(storagePort).upload(anyString(), any(), anyString());
        given(cadFileRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        CadFile result = service.upload(projectId, userId, stepFileCommand("rack.step", 1024));

        assertThat(result.status()).isEqualTo(CadFileStatus.READY);
        assertThat(result.originalFilename()).isEqualTo("rack.step");
        verify(storagePort).upload(anyString(), any(), anyString());
    }

    @Test
    void upload_marksFileAsFailed_whenStorageThrows() {
        given(projectAccessPort.isMember(projectId, userId)).willReturn(true);
        given(cadFileRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        willThrow(new RuntimeException("network error"))
            .given(storagePort).upload(anyString(), any(), anyString());

        assertThatThrownBy(() -> service.upload(projectId, userId, stepFileCommand("rack.step", 1024)))
            .isInstanceOf(CadFileStorageException.class);
    }

    @Test
    void upload_throwsAccessDenied_whenNotMember() {
        given(projectAccessPort.isMember(projectId, userId)).willReturn(false);

        assertThatThrownBy(() -> service.upload(projectId, userId, stepFileCommand("rack.step", 1024)))
            .isInstanceOf(ProjectAccessDeniedException.class);
        verify(storagePort, never()).upload(any(), any(), any());
    }

    @Test
    void upload_throwsInvalidCadFile_whenUnsupportedExtension() {
        given(projectAccessPort.isMember(projectId, userId)).willReturn(true);

        assertThatThrownBy(() -> service.upload(projectId, userId, stepFileCommand("layout.pdf", 512)))
            .isInstanceOf(InvalidCadFileException.class)
            .hasMessageContaining("Unsupported file type");
    }

    @Test
    void upload_throwsInvalidCadFile_whenFileTooLarge() {
        given(projectAccessPort.isMember(projectId, userId)).willReturn(true);
        long oversized = 600L * 1024 * 1024;

        assertThatThrownBy(() -> service.upload(projectId, userId, stepFileCommand("rack.step", oversized)))
            .isInstanceOf(InvalidCadFileException.class)
            .hasMessageContaining("maximum allowed size");
    }

    @Test
    void listByProject_returnsFiles_whenMember() {
        given(projectAccessPort.isMember(projectId, userId)).willReturn(true);
        given(cadFileRepository.findAllByProject(projectId)).willReturn(List.of(readyFile()));

        List<CadFile> result = service.listByProject(projectId, userId);

        assertThat(result).hasSize(1);
    }

    @Test
    void getFile_throwsNotFound_whenFileDoesNotExist() {
        given(projectAccessPort.isMember(projectId, userId)).willReturn(true);
        CadFileId fileId = new CadFileId(UUID.randomUUID());
        given(cadFileRepository.findById(fileId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.getFile(projectId, fileId, userId))
            .isInstanceOf(CadFileNotFoundException.class);
    }

    @Test
    void getDownloadUrl_returnsSignedUrl_whenFileExists() {
        given(projectAccessPort.isMember(projectId, userId)).willReturn(true);
        CadFile file = readyFile();
        given(cadFileRepository.findById(file.id())).willReturn(Optional.of(file));
        given(storagePort.generateSignedUrl(anyString(), any(Duration.class))).willReturn("https://signed-url");

        String url = service.getDownloadUrl(projectId, file.id(), userId);

        assertThat(url).isEqualTo("https://signed-url");
    }

    @Test
    void deleteFile_deletesFromStorageAndRepo_whenMemberAndFileExists() {
        given(projectAccessPort.isMember(projectId, userId)).willReturn(true);
        CadFile file = readyFile();
        given(cadFileRepository.findById(file.id())).willReturn(Optional.of(file));
        willDoNothing().given(storagePort).delete(anyString());

        service.deleteFile(projectId, file.id(), userId);

        verify(storagePort).delete(file.storagePath());
        verify(cadFileRepository).deleteById(file.id());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private UploadCommand stepFileCommand(String filename, long size) {
        return new UploadCommand(filename, "application/step", size, new byte[]{1, 2, 3});
    }

    private CadFile readyFile() {
        CadFileId id = new CadFileId(UUID.randomUUID());
        return new CadFile(id, projectId, userId, "rack.step", "application/step",
            2048, projectId.value() + "/" + id.value() + "_rack.step",
            CadFileStatus.READY, Instant.now());
    }
}
