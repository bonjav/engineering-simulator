package com.datacenterflow.simulation.adapter.out.temporal.dto;

public record ParsedCadData(
    String runId,
    String storagePath,
    String format,          // STEP, STL, OBJ, etc.
    int componentCount
) {}
