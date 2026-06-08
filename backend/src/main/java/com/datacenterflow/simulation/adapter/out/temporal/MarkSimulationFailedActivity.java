package com.datacenterflow.simulation.adapter.out.temporal;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface MarkSimulationFailedActivity {

    @ActivityMethod
    void markFailed(String runId, String errorMessage);
}
