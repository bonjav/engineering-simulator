package com.datacenterflow.simulation.adapter.out.engine;

import com.datacenterflow.simulation.adapter.out.temporal.dto.SimulationResult;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class SimulationEngineClient {

    private final RestClient restClient;

    public SimulationEngineClient(RestClient.Builder builder, FastApiProperties properties) {
        this.restClient = builder.baseUrl(properties.url()).build();
    }

    public SimulationResult runSimulation(String storagePath, String format) {
        FastApiResponse response = restClient.post()
            .uri("/api/simulate")
            .contentType(MediaType.APPLICATION_JSON)
            .body(Map.of("file_path", storagePath, "format", format))
            .retrieve()
            .body(FastApiResponse.class);

        if (response == null) {
            throw new IllegalStateException("Empty response from simulation engine");
        }
        return toResult(response);
    }

    private SimulationResult toResult(FastApiResponse r) {
        List<SimulationResult.AirflowZoneDto> zones = r.airflow_zones() == null ? List.of() :
            r.airflow_zones().stream()
                .map(z -> new SimulationResult.AirflowZoneDto(z.zone_id(), z.flow_rate_cfm(), z.status()))
                .toList();

        List<SimulationResult.HotspotDto> hotspots = r.hotspots() == null ? List.of() :
            r.hotspots().stream()
                .map(h -> new SimulationResult.HotspotDto(h.rack_id(), h.temp_celsius(), h.severity()))
                .toList();

        return new SimulationResult(
            zones,
            hotspots,
            r.cooling_recommendations() == null ? List.of() : r.cooling_recommendations(),
            r.energy_efficiency_score()
        );
    }

    // ── FastAPI response DTOs (snake_case from Python) ────────────────────────

    record FastApiResponse(
        List<AirflowZoneRaw> airflow_zones,
        List<HotspotRaw> hotspots,
        List<String> cooling_recommendations,
        double energy_efficiency_score
    ) {}

    record AirflowZoneRaw(String zone_id, double flow_rate_cfm, String status) {}
    record HotspotRaw(String rack_id, double temp_celsius, String severity) {}
}
