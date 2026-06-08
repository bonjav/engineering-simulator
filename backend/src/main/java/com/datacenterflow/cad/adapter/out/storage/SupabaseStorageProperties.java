package com.datacenterflow.cad.adapter.out.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "supabase.storage")
public record SupabaseStorageProperties(
    String bucket,
    int signedUrlExpirySeconds
) {}
