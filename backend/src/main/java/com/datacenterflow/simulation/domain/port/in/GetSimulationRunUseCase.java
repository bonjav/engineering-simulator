package com.datacenterflow.simulation.domain.port.in;

import com.datacenterflow.auth.domain.model.UserId;
import com.datacenterflow.project.domain.model.ProjectId;
import com.datacenterflow.simulation.domain.model.SimulationRun;
import com.datacenterflow.simulation.domain.model.SimulationRunId;

public interface GetSimulationRunUseCase {

    SimulationRun getSimulationRun(ProjectId projectId, SimulationRunId runId, UserId requesterId);
}
