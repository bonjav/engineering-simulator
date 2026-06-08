package com.datacenterflow.simulation.adapter.out.temporal;

import com.datacenterflow.cad.domain.model.CadFileId;
import com.datacenterflow.project.domain.model.ProjectId;
import com.datacenterflow.simulation.adapter.out.temporal.dto.SimulationInput;
import com.datacenterflow.simulation.domain.model.SimulationRunId;
import com.datacenterflow.simulation.domain.port.out.WorkflowLaunchPort;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
class TemporalWorkflowLaunchAdapter implements WorkflowLaunchPort {

    private final WorkflowClient workflowClient;

    TemporalWorkflowLaunchAdapter(WorkflowClient workflowClient) {
        this.workflowClient = workflowClient;
    }

    @Override
    public String launch(SimulationRunId runId, ProjectId projectId, CadFileId cadFileId, String storagePath) {
        String workflowId = "simulation-" + runId.value();

        SimulationWorkflow workflow = workflowClient.newWorkflowStub(
            SimulationWorkflow.class,
            WorkflowOptions.newBuilder()
                .setWorkflowId(workflowId)
                .setTaskQueue(SimulationWorkflow.TASK_QUEUE)
                .setWorkflowExecutionTimeout(Duration.ofHours(4))
                .setWorkflowRunTimeout(Duration.ofHours(2))
                .build()
        );

        WorkflowClient.start(workflow::run, new SimulationInput(
            runId.value().toString(),
            projectId.value().toString(),
            cadFileId.value().toString(),
            storagePath
        ));

        return workflowId;
    }
}
