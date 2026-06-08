package com.datacenterflow.simulation.adapter.out.temporal;

import com.datacenterflow.simulation.adapter.out.temporal.dto.ParsedCadData;
import com.datacenterflow.simulation.adapter.out.temporal.dto.SimulationResult;
import com.datacenterflow.simulation.adapter.out.engine.SimulationEngineClient;
import io.temporal.activity.Activity;
import org.springframework.stereotype.Component;

@Component
public class RunSimulationActivityImpl implements RunSimulationActivity {

    private final SimulationEngineClient engineClient;

    public RunSimulationActivityImpl(SimulationEngineClient engineClient) {
        this.engineClient = engineClient;
    }

    @Override
    public SimulationResult simulate(ParsedCadData cadData) {
        // Send heartbeat so Temporal knows this long-running activity is alive
        Activity.getExecutionContext().heartbeat("starting simulation for run=" + cadData.runId());
        SimulationResult result = engineClient.runSimulation(cadData.storagePath(), cadData.format());
        Activity.getExecutionContext().heartbeat("simulation complete");
        return result;
    }
}
