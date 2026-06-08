package com.datacenterflow.cad.domain.port.in;

import com.datacenterflow.auth.domain.model.UserId;
import com.datacenterflow.cad.domain.model.CadFileId;
import com.datacenterflow.project.domain.model.ProjectId;

public interface GetDownloadUrlUseCase {

    String getDownloadUrl(ProjectId projectId, CadFileId fileId, UserId requesterId);
}
