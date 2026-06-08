package com.datacenterflow.project.domain.port.in;

import com.datacenterflow.auth.domain.model.UserId;
import com.datacenterflow.project.domain.model.ProjectId;

public interface RemoveProjectMemberUseCase {

    void removeMember(ProjectId projectId, UserId requesterId, UserId targetUserId);
}
