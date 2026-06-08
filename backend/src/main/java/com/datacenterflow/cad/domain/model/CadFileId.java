package com.datacenterflow.cad.domain.model;

import java.util.Objects;
import java.util.UUID;

public record CadFileId(UUID value) {

    public CadFileId {
        Objects.requireNonNull(value, "CadFileId cannot be null");
    }

    public static CadFileId of(String value) {
        return new CadFileId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
