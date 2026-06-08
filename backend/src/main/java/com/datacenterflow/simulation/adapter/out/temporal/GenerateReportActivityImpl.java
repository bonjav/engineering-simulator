package com.datacenterflow.simulation.adapter.out.temporal;

import com.datacenterflow.simulation.adapter.out.temporal.dto.SimulationResult;
import com.datacenterflow.simulation.domain.model.SimulationReport;
import com.datacenterflow.simulation.domain.model.SimulationRunId;
import com.datacenterflow.simulation.domain.port.out.SimulationRunRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class GenerateReportActivityImpl implements GenerateReportActivity {

    private final SimulationRunRepository runRepository;

    public GenerateReportActivityImpl(SimulationRunRepository runRepository) {
        this.runRepository = runRepository;
    }

    @Override
    public SimulationReport generate(String runId, SimulationResult result) {
        SimulationReport report = toReport(result);

        runRepository.findById(SimulationRunId.of(runId))
            .ifPresent(run -> runRepository.save(run.withReport(report)));

        return report;
    }

    private SimulationReport toReport(SimulationResult r) {
        List<SimulationReport.AirflowZone> zones = r.airflowZones().stream()
            .map(z -> new SimulationReport.AirflowZone(z.zoneId(), z.flowRateCfm(), z.status()))
            .toList();

        List<SimulationReport.HotspotPrediction> hotspots = r.hotspots().stream()
            .map(h -> new SimulationReport.HotspotPrediction(h.rackId(), h.tempCelsius(), h.severity()))
            .toList();

        return new SimulationReport(zones, hotspots, r.coolingRecommendations(),
            r.energyEfficiencyScore(), Instant.now());
    }
}
