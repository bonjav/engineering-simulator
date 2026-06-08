package com.datacenterflow.simulation.adapter.out.temporal;

import com.datacenterflow.simulation.adapter.out.temporal.dto.ParsedCadData;
import com.datacenterflow.simulation.adapter.out.temporal.dto.SimulationResult;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface RunSimulationActivity {

    @ActivityMethod
    SimulationResult simulate(ParsedCadData cadData);
}
