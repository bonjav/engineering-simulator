package com.datacenterflow.simulation.domain.exception;

import com.datacenterflow.simulation.domain.model.SimulationRunId;

public class SimulationRunNotFoundException extends RuntimeException {

    public SimulationRunNotFoundException(SimulationRunId id) {
        super("Simulation run not found: " + id.value());
    }
}
