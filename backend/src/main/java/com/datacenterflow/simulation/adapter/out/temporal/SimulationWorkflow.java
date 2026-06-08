package com.datacenterflow.simulation.adapter.out.temporal;

import com.datacenterflow.simulation.adapter.out.temporal.dto.SimulationInput;
import com.datacenterflow.simulation.domain.model.SimulationReport;
import com.datacenterflow.simulation.domain.model.SimulationStatus;
import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface SimulationWorkflow {

    String TASK_QUEUE = "simulation-task-queue";

    @WorkflowMethod
    SimulationReport run(SimulationInput input);

    @QueryMethod
    SimulationStatus getStatus();
}
