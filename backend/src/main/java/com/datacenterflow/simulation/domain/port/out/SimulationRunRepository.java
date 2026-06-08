package com.datacenterflow.simulation.domain.port.out;

import com.datacenterflow.project.domain.model.ProjectId;
import com.datacenterflow.simulation.domain.model.SimulationRun;
import com.datacenterflow.simulation.domain.model.SimulationRunId;

import java.util.List;
import java.util.Optional;

public interface SimulationRunRepository {

    Optional<SimulationRun> findById(SimulationRunId id);

    List<SimulationRun> findAllByProject(ProjectId projectId);

    SimulationRun save(SimulationRun run);
}
