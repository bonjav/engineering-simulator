package com.datacenterflow.simulation.domain.model;

import java.time.Instant;
import java.util.List;

public record SimulationReport(
    List<AirflowZone> airflowZones,
    List<HotspotPrediction> hotspots,
    List<String> coolingRecommendations,
    double energyEfficiencyScore,
    Instant generatedAt
) {

    public record AirflowZone(
        String zoneId,
        double flowRateCfm,
        String status         // OK, WARNING, CRITICAL
    ) {}

    public record HotspotPrediction(
        String rackId,
        double tempCelsius,
        String severity       // LOW, MEDIUM, HIGH, CRITICAL
    ) {}
}
