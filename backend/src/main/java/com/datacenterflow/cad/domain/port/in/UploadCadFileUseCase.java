package com.datacenterflow.cad.domain.port.in;

import com.datacenterflow.auth.domain.model.UserId;
import com.datacenterflow.cad.domain.model.CadFile;
import com.datacenterflow.project.domain.model.ProjectId;

public interface UploadCadFileUseCase {

    CadFile upload(ProjectId projectId, UserId uploadedBy, UploadCommand command);

    record UploadCommand(
        String originalFilename,
        String contentType,
        long fileSizeBytes,
        byte[] content
    ) {}
}
