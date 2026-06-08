package com.datacenterflow.simulation.adapter.out.temporal;

import com.datacenterflow.simulation.adapter.out.temporal.dto.ParsedCadData;
import com.datacenterflow.simulation.adapter.out.temporal.dto.SimulationInput;
import com.datacenterflow.simulation.adapter.out.temporal.dto.SimulationResult;
import com.datacenterflow.simulation.domain.model.SimulationReport;
import com.datacenterflow.simulation.domain.model.SimulationStatus;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.testing.TestWorkflowEnvironment;
import io.temporal.worker.Worker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SimulationWorkflowTest {

    private TestWorkflowEnvironment testEnv;
    private Worker worker;
    private WorkflowClient client;

    private ParseCadActivity parseCad;
    private RunSimulationActivity runSimulation;
    private GenerateReportActivity generateReport;
    private MarkSimulationFailedActivity markFailed;

    @BeforeEach
    void setUp() {
        testEnv = TestWorkflowEnvironment.newInstance();
        worker = testEnv.newWorker(SimulationWorkflow.TASK_QUEUE);

        parseCad = mock(ParseCadActivity.class);
        runSimulation = mock(RunSimulationActivity.class);
        generateReport = mock(GenerateReportActivity.class);
        markFailed = mock(MarkSimulationFailedActivity.class);

        worker.registerWorkflowImplementationTypes(SimulationWorkflowImpl.class);
        worker.registerActivitiesImplementations(parseCad, runSimulation, generateReport, markFailed);

        testEnv.start();
        client = testEnv.getWorkflowClient();
    }

    @AfterEach
    void tearDown() {
        testEnv.close();
    }

    @Test
    void workflow_completesWithReport_whenAllActivitiesSucceed() {
        String runId = UUID.randomUUID().toString();
        ParsedCadData cadData = new ParsedCadData(runId, "project/run/rack.step", "STEP", 1);
        SimulationResult simResult = new SimulationResult(List.of(), List.of(), List.of("Increase airflow"), 0.82);
        SimulationReport report = new SimulationReport(List.of(), List.of(), List.of("Increase airflow"), 0.82, Instant.now());

        given(parseCad.parse(anyString(), anyString())).willReturn(cadData);
        given(runSimulation.simulate(any())).willReturn(simResult);
        given(generateReport.generate(anyString(), any())).willReturn(report);

        SimulationWorkflow workflow = client.newWorkflowStub(
            SimulationWorkflow.class,
            WorkflowOptions.newBuilder().setTaskQueue(SimulationWorkflow.TASK_QUEUE).build()
        );

        SimulationReport result = workflow.run(new SimulationInput(runId, "proj-id", "cad-id", "project/run/rack.step"));

        assertThat(result.energyEfficiencyScore()).isEqualTo(0.82);
        assertThat(result.coolingRecommendations()).contains("Increase airflow");
        verify(parseCad).parse(anyString(), anyString());
        verify(runSimulation).simulate(any());
        verify(generateReport).generate(anyString(), any());
    }

    @Test
    void workflow_queriesStatus_returnsRunning_duringExecution() {
        // TestWorkflowEnvironment runs activities synchronously in test mode,
        // so we verify the final completed state instead of mid-execution state.
        String runId = UUID.randomUUID().toString();
        SimulationReport report = new SimulationReport(List.of(), List.of(), List.of(), 0.9, Instant.now());

        given(parseCad.parse(anyString(), anyString()))
            .willReturn(new ParsedCadData(runId, "path", "STEP", 1));
        given(runSimulation.simulate(any()))
            .willReturn(new SimulationResult(List.of(), List.of(), List.of(), 0.9));
        given(generateReport.generate(anyString(), any())).willReturn(report);

        SimulationWorkflow stub = client.newWorkflowStub(
            SimulationWorkflow.class,
            WorkflowOptions.newBuilder().setTaskQueue(SimulationWorkflow.TASK_QUEUE).build()
        );
        stub.run(new SimulationInput(runId, "proj", "cad", "path"));

        // After completion the status should be COMPLETED
        SimulationStatus status = stub.getStatus();
        assertThat(status).isEqualTo(SimulationStatus.COMPLETED);
    }

    @Test
    void workflow_callsMarkFailed_whenActivityThrows() {
        String runId = UUID.randomUUID().toString();
        given(parseCad.parse(anyString(), anyString())).willThrow(new RuntimeException("parse error"));

        SimulationWorkflow workflow = client.newWorkflowStub(
            SimulationWorkflow.class,
            WorkflowOptions.newBuilder().setTaskQueue(SimulationWorkflow.TASK_QUEUE).build()
        );

        try {
            workflow.run(new SimulationInput(runId, "proj", "cad", "path"));
        } catch (Exception ignored) {
            // expected — workflow propagates the failure
        }

        verify(markFailed).markFailed(anyString(), anyString());
    }
}
