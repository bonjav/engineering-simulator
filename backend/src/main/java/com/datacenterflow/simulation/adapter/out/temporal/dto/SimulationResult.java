package com.datacenterflow.simulation.adapter.out.temporal.dto;

import java.util.List;

public record SimulationResult(
    List<AirflowZoneDto> airflowZones,
    List<HotspotDto> hotspots,
    List<String> coolingRecommendations,
    double energyEfficiencyScore
) {

    public record AirflowZoneDto(String zoneId, double flowRateCfm, String status) {}
    public record HotspotDto(String rackId, double tempCelsius, String severity) {}
}
