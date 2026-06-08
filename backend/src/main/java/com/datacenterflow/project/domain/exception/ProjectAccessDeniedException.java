package com.datacenterflow.project.domain.exception;

import com.datacenterflow.auth.domain.model.UserId;
import com.datacenterflow.project.domain.model.ProjectId;

public class ProjectAccessDeniedException extends RuntimeException {

    public ProjectAccessDeniedException(UserId userId, ProjectId projectId) {
        super("User " + userId + " does not have access to project " + projectId);
    }
}
