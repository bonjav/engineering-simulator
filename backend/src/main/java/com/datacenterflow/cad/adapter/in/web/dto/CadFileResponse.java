package com.datacenterflow.cad.adapter.in.web.dto;

import com.datacenterflow.cad.domain.model.CadFile;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "CAD file metadata")
public record CadFileResponse(

    @Schema(description = "File UUID") String id,
    @Schema(description = "Owning project UUID") String projectId,
    @Schema(description = "Uploader user UUID") String uploadedBy,
    @Schema(description = "Original filename", example = "rack-layout-q3.step") String originalFilename,
    @Schema(description = "MIME type", example = "application/step") String contentType,
    @Schema(description = "File size in bytes") long fileSizeBytes,
    @Schema(description = "Human-readable file size", example = "2.4 MB") String fileSizeHuman,
    @Schema(description = "Storage object path") String storagePath,
    @Schema(description = "Upload status", example = "READY") String status,
    @Schema(description = "Upload timestamp") Instant createdAt
) {

    public static CadFileResponse from(CadFile f) {
        return new CadFileResponse(
            f.id().value().toString(),
            f.projectId().value().toString(),
            f.uploadedBy().value().toString(),
            f.originalFilename(),
            f.contentType(),
            f.fileSizeBytes(),
            humanReadableSize(f.fileSizeBytes()),
            f.storagePath(),
            f.status().name(),
            f.createdAt()
        );
    }

    private static String humanReadableSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }
}
