package com.datacenterflow.simulation.domain.port.out;

import com.datacenterflow.cad.domain.model.CadFileId;

import java.util.Optional;

public interface CadFileAccessPort {

    Optional<String> getStoragePath(CadFileId cadFileId);
}
