package com.datacenterflow.project.domain.port.in;

import com.datacenterflow.auth.domain.model.UserId;
import com.datacenterflow.project.domain.model.Project;

import java.util.List;

public interface ListProjectsUseCase {

    List<Project> listAccessibleProjects(UserId userId);
}
