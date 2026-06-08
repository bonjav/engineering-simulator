package com.datacenterflow.project.domain.port.in;

import com.datacenterflow.auth.domain.model.UserId;
import com.datacenterflow.project.domain.model.Project;
import com.datacenterflow.project.domain.model.ProjectId;

public interface GetProjectUseCase {

    Project getProject(ProjectId projectId, UserId requesterId);
}
