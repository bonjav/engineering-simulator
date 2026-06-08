package com.datacenterflow.simulation.adapter.out.crossdomain;

import com.datacenterflow.auth.domain.model.UserId;
import com.datacenterflow.project.domain.model.ProjectId;
import com.datacenterflow.project.domain.port.out.ProjectMemberRepository;
import com.datacenterflow.simulation.domain.port.out.ProjectMembershipPort;
import org.springframework.stereotype.Component;

@Component
class SimulationProjectMembershipAdapter implements ProjectMembershipPort {

    private final ProjectMemberRepository memberRepository;

    SimulationProjectMembershipAdapter(ProjectMemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public boolean isMember(ProjectId projectId, UserId userId) {
        return memberRepository.findByProjectAndUser(projectId, userId).isPresent();
    }
}
