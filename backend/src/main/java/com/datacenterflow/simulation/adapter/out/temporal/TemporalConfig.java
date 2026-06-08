package com.datacenterflow.simulation.adapter.out.temporal;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class TemporalConfig {

    @Value("${temporal.host:localhost:7233}")
    private String temporalHost;

    @Value("${temporal.namespace:default}")
    private String namespace;

    @Bean
    public WorkflowServiceStubs workflowServiceStubs() {
        return WorkflowServiceStubs.newConnectedServiceStubs(
            WorkflowServiceStubsOptions.newBuilder()
                .setTarget(temporalHost)
                .build(),
            Duration.ofSeconds(10)
        );
    }

    @Bean
    public WorkflowClient workflowClient(WorkflowServiceStubs stubs) {
        return WorkflowClient.newInstance(stubs,
            WorkflowClientOptions.newBuilder().setNamespace(namespace).build());
    }

    @Bean
    public WorkerFactory workerFactory(WorkflowClient client) {
        return WorkerFactory.newInstance(client);
    }

    @Bean
    public Worker simulationWorker(
        WorkerFactory factory,
        ParseCadActivityImpl parseCad,
        RunSimulationActivityImpl runSim,
        GenerateReportActivityImpl generateReport,
        MarkSimulationFailedActivityImpl markFailed
    ) {
        Worker worker = factory.newWorker(SimulationWorkflow.TASK_QUEUE);
        worker.registerWorkflowImplementationTypes(SimulationWorkflowImpl.class);
        worker.registerActivitiesImplementations(parseCad, runSim, generateReport, markFailed);
        factory.start();
        return worker;
    }
}
