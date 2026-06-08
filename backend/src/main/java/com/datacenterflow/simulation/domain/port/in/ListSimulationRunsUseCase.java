package com.datacenterflow.simulation.domain.port.in;

import com.datacenterflow.auth.domain.model.UserId;
import com.datacenterflow.project.domain.model.ProjectId;
import com.datacenterflow.simulation.domain.model.SimulationRun;

import java.util.List;

public interface ListSimulationRunsUseCase {

    List<SimulationRun> listByProject(ProjectId projectId, UserId requesterId);
}
