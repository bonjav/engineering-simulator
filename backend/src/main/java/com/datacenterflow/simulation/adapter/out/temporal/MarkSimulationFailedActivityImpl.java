package com.datacenterflow.simulation.adapter.out.temporal;

import com.datacenterflow.simulation.domain.model.SimulationRunId;
import com.datacenterflow.simulation.domain.port.out.SimulationRunRepository;
import org.springframework.stereotype.Component;

@Component
public class MarkSimulationFailedActivityImpl implements MarkSimulationFailedActivity {

    private final SimulationRunRepository runRepository;

    public MarkSimulationFailedActivityImpl(SimulationRunRepository runRepository) {
        this.runRepository = runRepository;
    }

    @Override
    public void markFailed(String runId, String errorMessage) {
        runRepository.findById(SimulationRunId.of(runId))
            .ifPresent(run -> runRepository.save(run.withError(errorMessage)));
    }
}
