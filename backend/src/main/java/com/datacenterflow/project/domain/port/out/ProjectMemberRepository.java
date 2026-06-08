package com.datacenterflow.project.domain.port.out;

import com.datacenterflow.auth.domain.model.UserId;
import com.datacenterflow.project.domain.model.ProjectId;
import com.datacenterflow.project.domain.model.ProjectMember;

import java.util.List;
import java.util.Optional;

public interface ProjectMemberRepository {

    Optional<ProjectMember> findByProjectAndUser(ProjectId projectId, UserId userId);

    List<ProjectMember> findAllByProject(ProjectId projectId);

    ProjectMember save(ProjectMember member);

    void delete(ProjectId projectId, UserId userId);
}
