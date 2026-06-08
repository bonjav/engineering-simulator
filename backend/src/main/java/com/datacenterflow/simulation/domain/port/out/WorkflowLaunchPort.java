package com.datacenterflow.simulation.domain.port.out;

import com.datacenterflow.simulation.domain.model.SimulationRunId;
import com.datacenterflow.project.domain.model.ProjectId;
import com.datacenterflow.cad.domain.model.CadFileId;

public interface WorkflowLaunchPort {

    /**
     * Starts the simulation workflow asynchronously.
     * Returns the Temporal workflow ID assigned to this run.
     */
    String launch(SimulationRunId runId, ProjectId projectId, CadFileId cadFileId, String storagePath);
}
