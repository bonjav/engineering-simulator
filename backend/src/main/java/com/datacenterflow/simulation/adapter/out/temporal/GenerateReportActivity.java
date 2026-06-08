package com.datacenterflow.simulation.adapter.out.temporal;

import com.datacenterflow.simulation.adapter.out.temporal.dto.SimulationResult;
import com.datacenterflow.simulation.domain.model.SimulationReport;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface GenerateReportActivity {

    @ActivityMethod
    SimulationReport generate(String runId, SimulationResult result);
}
