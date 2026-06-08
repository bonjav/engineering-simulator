package com.datacenterflow.project.domain.port.in;

import com.datacenterflow.auth.domain.model.UserId;
import com.datacenterflow.project.domain.model.Project;
import com.datacenterflow.project.domain.model.ProjectId;

public interface UpdateProjectUseCase {

    Project updateProject(ProjectId projectId, UserId requesterId, UpdateProjectCommand command);

    record UpdateProjectCommand(String name, String description) {}
}
