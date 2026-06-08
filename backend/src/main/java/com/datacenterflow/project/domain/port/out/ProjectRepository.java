package com.datacenterflow.project.domain.port.out;

import com.datacenterflow.auth.domain.model.UserId;
import com.datacenterflow.project.domain.model.Project;
import com.datacenterflow.project.domain.model.ProjectId;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository {

    Optional<Project> findById(ProjectId id);

    List<Project> findAllAccessibleByUser(UserId userId);

    Project save(Project project);

    void deleteById(ProjectId id);
}
