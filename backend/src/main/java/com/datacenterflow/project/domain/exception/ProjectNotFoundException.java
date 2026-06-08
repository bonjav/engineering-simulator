package com.datacenterflow.project.domain.exception;

import com.datacenterflow.project.domain.model.ProjectId;

public class ProjectNotFoundException extends RuntimeException {

    public ProjectNotFoundException(ProjectId id) {
        super("Project not found: " + id.value());
    }
}
