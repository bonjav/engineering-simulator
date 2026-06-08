package com.datacenterflow.simulation.adapter.out.temporal;

import com.datacenterflow.simulation.adapter.out.temporal.dto.ParsedCadData;
import com.datacenterflow.simulation.domain.model.SimulationRunId;
import com.datacenterflow.simulation.domain.model.SimulationStatus;
import com.datacenterflow.simulation.domain.port.out.SimulationRunRepository;
import org.springframework.stereotype.Component;

@Component
public class ParseCadActivityImpl implements ParseCadActivity {

    private final SimulationRunRepository runRepository;

    public ParseCadActivityImpl(SimulationRunRepository runRepository) {
        this.runRepository = runRepository;
    }

    @Override
    public ParsedCadData parse(String storagePath, String runId) {
        // Mark the run as RUNNING now that processing has started
        runRepository.findById(SimulationRunId.of(runId))
            .ifPresent(run -> runRepository.save(run.withStatus(SimulationStatus.RUNNING)));

        String format = extractFormat(storagePath);
        // Component count would come from an actual CAD parser; use 1 as a safe default
        return new ParsedCadData(runId, storagePath, format, 1);
    }

    private String extractFormat(String storagePath) {
        String lower = storagePath.toLowerCase();
        if (lower.contains(".step") || lower.contains(".stp")) return "STEP";
        if (lower.contains(".iges") || lower.contains(".igs")) return "IGES";
        if (lower.contains(".stl")) return "STL";
        if (lower.contains(".obj")) return "OBJ";
        return "UNKNOWN";
    }
}
