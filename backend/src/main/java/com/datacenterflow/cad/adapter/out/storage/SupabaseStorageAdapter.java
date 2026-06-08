package com.datacenterflow.cad.adapter.out.storage;

import com.datacenterflow.cad.domain.exception.CadFileStorageException;
import com.datacenterflow.cad.domain.port.out.StoragePort;
import com.datacenterflow.infrastructure.security.SupabaseProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
class SupabaseStorageAdapter implements StoragePort {

    private final RestClient restClient;
    private final SupabaseStorageProperties storageProperties;
    private final SupabaseProperties supabaseProperties;

    SupabaseStorageAdapter(
        RestClient.Builder restClientBuilder,
        SupabaseStorageProperties storageProperties,
        SupabaseProperties supabaseProperties
    ) {
        this.storageProperties = storageProperties;
        this.supabaseProperties = supabaseProperties;
        this.restClient = restClientBuilder
            .baseUrl(supabaseProperties.url())
            .build();
    }

    @Override
    public void upload(String path, byte[] content, String contentType) {
        try {
            restClient.post()
                .uri("/storage/v1/object/{bucket}/{path}", storageProperties.bucket(), path)
                .header("Authorization", bearer())
                .header("x-upsert", "true")
                .contentType(MediaType.parseMediaType(contentType))
                .body(content)
                .retrieve()
                .toBodilessEntity();
        } catch (RestClientException e) {
            throw new CadFileStorageException("Upload to Supabase Storage failed for path: " + path, e);
        }
    }

    @Override
    public void delete(String path) {
        try {
            restClient.method(org.springframework.http.HttpMethod.DELETE)
                .uri("/storage/v1/object/{bucket}", storageProperties.bucket())
                .header("Authorization", bearer())
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("prefixes", List.of(path)))
                .retrieve()
                .toBodilessEntity();
        } catch (RestClientException e) {
            throw new CadFileStorageException("Delete from Supabase Storage failed for path: " + path, e);
        }
    }

    @Override
    public String generateSignedUrl(String path, Duration ttl) {
        try {
            SignedUrlResponse response = restClient.post()
                .uri("/storage/v1/object/sign/{bucket}/{path}", storageProperties.bucket(), path)
                .header("Authorization", bearer())
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("expiresIn", ttl.toSeconds()))
                .retrieve()
                .body(SignedUrlResponse.class);

            if (response == null || response.signedURL() == null) {
                throw new CadFileStorageException("Empty signed URL response for path: " + path, null);
            }
            return supabaseProperties.url() + response.signedURL();
        } catch (RestClientException e) {
            throw new CadFileStorageException("Failed to generate signed URL for path: " + path, e);
        }
    }

    private String bearer() {
        return "Bearer " + supabaseProperties.serviceRoleKey();
    }

    record SignedUrlResponse(String signedURL) {}
}
