package com.datacenterflow.project.domain.port.in;

import com.datacenterflow.auth.domain.model.UserId;
import com.datacenterflow.project.domain.model.ProjectId;
import com.datacenterflow.project.domain.model.ProjectMember;
import com.datacenterflow.project.domain.model.ProjectRole;

public interface AddProjectMemberUseCase {

    ProjectMember addMember(ProjectId projectId, UserId requesterId, AddMemberCommand command);

    record AddMemberCommand(UserId userId, ProjectRole role) {}
}
