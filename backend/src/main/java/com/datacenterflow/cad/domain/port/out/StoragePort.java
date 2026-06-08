package com.datacenterflow.cad.domain.port.out;

import java.time.Duration;

public interface StoragePort {

    void upload(String path, byte[] content, String contentType);

    void delete(String path);

    String generateSignedUrl(String path, Duration ttl);
}
