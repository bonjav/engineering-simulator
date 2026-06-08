package com.datacenterflow.cad.domain.exception;

import com.datacenterflow.cad.domain.model.CadFileId;

public class CadFileNotFoundException extends RuntimeException {

    public CadFileNotFoundException(CadFileId id) {
        super("CAD file not found: " + id.value());
    }
}
