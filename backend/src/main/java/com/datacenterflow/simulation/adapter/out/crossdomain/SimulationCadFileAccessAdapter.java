package com.datacenterflow.simulation.adapter.out.crossdomain;

import com.datacenterflow.cad.domain.model.CadFileId;
import com.datacenterflow.cad.domain.port.out.CadFileRepository;
import com.datacenterflow.simulation.domain.port.out.CadFileAccessPort;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
class SimulationCadFileAccessAdapter implements CadFileAccessPort {

    private final CadFileRepository cadFileRepository;

    SimulationCadFileAccessAdapter(CadFileRepository cadFileRepository) {
        this.cadFileRepository = cadFileRepository;
    }

    @Override
    public Optional<String> getStoragePath(CadFileId cadFileId) {
        return cadFileRepository.findById(cadFileId).map(f -> f.storagePath());
    }
}
