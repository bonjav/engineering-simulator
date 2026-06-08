package com.datacenterflow.simulation.domain.port.in;

import com.datacenterflow.auth.domain.model.UserId;
import com.datacenterflow.cad.domain.model.CadFileId;
import com.datacenterflow.project.domain.model.ProjectId;
import com.datacenterflow.simulation.domain.model.SimulationRun;

public interface StartSimulationUseCase {

    SimulationRun startSimulation(ProjectId projectId, UserId requestedBy, StartSimulationCommand command);

    record StartSimulationCommand(CadFileId cadFileId) {}
}
