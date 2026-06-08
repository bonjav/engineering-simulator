package com.datacenterflow.cad.domain.port.in;

import com.datacenterflow.auth.domain.model.UserId;
import com.datacenterflow.cad.domain.model.CadFile;
import com.datacenterflow.project.domain.model.ProjectId;

import java.util.List;

public interface ListCadFilesUseCase {

    List<CadFile> listByProject(ProjectId projectId, UserId requesterId);
}
