package com.datacenterflow.project.domain.port.in;

import com.datacenterflow.auth.domain.model.UserId;
import com.datacenterflow.project.domain.model.Project;

public interface CreateProjectUseCase {

    Project createProject(UserId ownerId, CreateProjectCommand command);

    record CreateProjectCommand(String name, String description) {}
}
