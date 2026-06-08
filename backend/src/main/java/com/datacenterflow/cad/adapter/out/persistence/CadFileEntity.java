package com.datacenterflow.cad.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cad_files")
class CadFileEntity {

    @Id
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "project_id", columnDefinition = "uuid", nullable = false)
    private UUID projectId;

    @Column(name = "uploaded_by", columnDefinition = "uuid", nullable = false)
    private UUID uploadedBy;

    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "file_size_bytes", nullable = false)
    private long fileSizeBytes;

    @Column(name = "storage_path", nullable = false, length = 500)
    private String storagePath;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected CadFileEntity() {}

    CadFileEntity(UUID id, UUID projectId, UUID uploadedBy, String originalFilename,
                  String contentType, long fileSizeBytes, String storagePath, String status) {
        this.id = id;
        this.projectId = projectId;
        this.uploadedBy = uploadedBy;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.fileSizeBytes = fileSizeBytes;
        this.storagePath = storagePath;
        this.status = status;
    }

    UUID getId() { return id; }
    UUID getProjectId() { return projectId; }
    UUID getUploadedBy() { return uploadedBy; }
    String getOriginalFilename() { return originalFilename; }
    String getContentType() { return contentType; }
    long getFileSizeBytes() { return fileSizeBytes; }
    String getStoragePath() { return storagePath; }
    String getStatus() { return status; }
    Instant getCreatedAt() { return createdAt; }

    void setStatus(String status) { this.status = status; }
}
