package com.datacenterflow.cad.domain.model;

import com.datacenterflow.auth.domain.model.UserId;
import com.datacenterflow.project.domain.model.ProjectId;

import java.time.Instant;

public record CadFile(
    CadFileId id,
    ProjectId projectId,
    UserId uploadedBy,
    String originalFilename,
    String contentType,
    long fileSizeBytes,
    String storagePath,
    CadFileStatus status,
    Instant createdAt
) {

    public CadFile withStatus(CadFileStatus newStatus) {
        return new CadFile(id, projectId, uploadedBy, originalFilename,
            contentType, fileSizeBytes, storagePath, newStatus, createdAt);
    }
}
