package com.datacenterflow.project.domain.port.in;

import com.datacenterflow.auth.domain.model.UserId;
import com.datacenterflow.project.domain.model.ProjectId;
import com.datacenterflow.project.domain.model.ProjectMember;

import java.util.List;

public interface ListProjectMembersUseCase {

    List<ProjectMember> listMembers(ProjectId projectId, UserId requesterId);
}
