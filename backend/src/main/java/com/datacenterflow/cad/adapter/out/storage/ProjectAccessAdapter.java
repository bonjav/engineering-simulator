package com.datacenterflow.cad.adapter.out.storage;

import com.datacenterflow.auth.domain.model.UserId;
import com.datacenterflow.cad.domain.port.out.ProjectAccessPort;
import com.datacenterflow.project.domain.port.out.ProjectMemberRepository;
import com.datacenterflow.project.domain.model.ProjectId;
import org.springframework.stereotype.Component;

@Component
class ProjectAccessAdapter implements ProjectAccessPort {

    private final ProjectMemberRepository memberRepository;

    ProjectAccessAdapter(ProjectMemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public boolean isMember(ProjectId projectId, UserId userId) {
        return memberRepository.findByProjectAndUser(projectId, userId).isPresent();
    }
}
