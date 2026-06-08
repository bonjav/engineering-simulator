package com.datacenterflow.simulation.adapter.out.temporal.dto;

public record SimulationInput(
    String runId,
    String projectId,
    String cadFileId,
    String storagePath
) {}
