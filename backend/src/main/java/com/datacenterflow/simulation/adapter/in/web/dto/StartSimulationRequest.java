package com.datacenterflow.simulation.adapter.in.web.dto;

import com.datacenterflow.cad.domain.model.CadFileId;
import com.datacenterflow.simulation.domain.port.in.StartSimulationUseCase.StartSimulationCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Request to start a simulation run for a CAD file")
public record StartSimulationRequest(

    @NotNull
    @Schema(description = "UUID of the CAD file to simulate", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    UUID cadFileId
) {

    public StartSimulationCommand toCommand() {
        return new StartSimulationCommand(new CadFileId(cadFileId));
    }
}
