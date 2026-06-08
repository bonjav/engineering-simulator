package com.datacenterflow.simulation.adapter.out.temporal;

import com.datacenterflow.simulation.adapter.out.temporal.dto.ParsedCadData;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface ParseCadActivity {

    @ActivityMethod
    ParsedCadData parse(String storagePath, String runId);
}
