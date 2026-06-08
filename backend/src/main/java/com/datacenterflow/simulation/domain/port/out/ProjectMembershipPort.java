package com.datacenterflow.simulation.domain.port.out;

import com.datacenterflow.auth.domain.model.UserId;
import com.datacenterflow.project.domain.model.ProjectId;

public interface ProjectMembershipPort {

    boolean isMember(ProjectId projectId, UserId userId);
}
