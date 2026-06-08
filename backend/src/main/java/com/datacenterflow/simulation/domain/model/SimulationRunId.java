package com.datacenterflow.simulation.domain.model;

import java.util.Objects;
import java.util.UUID;

public record SimulationRunId(UUID value) {

    public SimulationRunId {
        Objects.requireNonNull(value, "SimulationRunId cannot be null");
    }

    public static SimulationRunId of(String value) {
        return new SimulationRunId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
