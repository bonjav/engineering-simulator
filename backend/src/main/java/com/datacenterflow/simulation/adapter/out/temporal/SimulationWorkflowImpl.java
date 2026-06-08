package com.datacenterflow.simulation.adapter.out.temporal;

import com.datacenterflow.simulation.adapter.out.temporal.dto.ParsedCadData;
import com.datacenterflow.simulation.adapter.out.temporal.dto.SimulationInput;
import com.datacenterflow.simulation.adapter.out.temporal.dto.SimulationResult;
import com.datacenterflow.simulation.domain.model.SimulationReport;
import com.datacenterflow.simulation.domain.model.SimulationStatus;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.failure.ActivityFailure;
import io.temporal.workflow.Workflow;

import java.time.Duration;

public class SimulationWorkflowImpl implements SimulationWorkflow {

    private SimulationStatus currentStatus = SimulationStatus.PENDING;

    private final ParseCadActivity parseCad = Workflow.newActivityStub(
        ParseCadActivity.class,
        ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofMinutes(5))
            .setRetryOptions(RetryOptions.newBuilder()
                .setMaximumAttempts(3)
                .setInitialInterval(Duration.ofSeconds(1))
                .setBackoffCoefficient(2.0)
                .build())
            .build()
    );

    private final RunSimulationActivity runSimulation = Workflow.newActivityStub(
        RunSimulationActivity.class,
        ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofMinutes(30))
            .setScheduleToCloseTimeout(Duration.ofHours(2))
            .setHeartbeatTimeout(Duration.ofMinutes(5))
            .setRetryOptions(RetryOptions.newBuilder()
                .setMaximumAttempts(2)
                .setInitialInterval(Duration.ofSeconds(30))
                .setBackoffCoefficient(2.0)
                .build())
            .build()
    );

    private final GenerateReportActivity generateReport = Workflow.newActivityStub(
        GenerateReportActivity.class,
        ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofMinutes(5))
            .setRetryOptions(RetryOptions.newBuilder()
                .setMaximumAttempts(3)
                .setInitialInterval(Duration.ofSeconds(2))
                .setBackoffCoefficient(2.0)
                .build())
            .build()
    );

    // Must not retry — failure record must be written exactly once
    private final MarkSimulationFailedActivity markFailed = Workflow.newActivityStub(
        MarkSimulationFailedActivity.class,
        ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofMinutes(2))
            .setRetryOptions(RetryOptions.newBuilder()
                .setMaximumAttempts(5)
                .setInitialInterval(Duration.ofSeconds(1))
                .build())
            .build()
    );

    @Override
    public SimulationReport run(SimulationInput input) {
        currentStatus = SimulationStatus.RUNNING;
        try {
            ParsedCadData cadData = parseCad.parse(input.storagePath(), input.runId());
            SimulationResult result = runSimulation.simulate(cadData);
            SimulationReport report = generateReport.generate(input.runId(), result);
            currentStatus = SimulationStatus.COMPLETED;
            return report;
        } catch (ActivityFailure e) {
            currentStatus = SimulationStatus.FAILED;
            String reason = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            markFailed.markFailed(input.runId(), reason);
            throw e;
        }
    }

    @Override
    public SimulationStatus getStatus() {
        return currentStatus;
    }
}
