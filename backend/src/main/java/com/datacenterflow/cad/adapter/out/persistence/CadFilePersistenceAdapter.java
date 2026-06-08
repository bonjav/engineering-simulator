package com.datacenterflow.cad.adapter.out.persistence;

import com.datacenterflow.auth.domain.model.UserId;
import com.datacenterflow.cad.domain.model.CadFile;
import com.datacenterflow.cad.domain.model.CadFileId;
import com.datacenterflow.cad.domain.model.CadFileStatus;
import com.datacenterflow.cad.domain.port.out.CadFileRepository;
import com.datacenterflow.project.domain.model.ProjectId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
class CadFilePersistenceAdapter implements CadFileRepository {

    private final CadFileJpaRepository jpaRepository;

    CadFilePersistenceAdapter(CadFileJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<CadFile> findById(CadFileId id) {
        return jpaRepository.findById(id.value()).map(this::toDomain);
    }

    @Override
    public List<CadFile> findAllByProject(ProjectId projectId) {
        return jpaRepository.findAllByProjectIdOrderByCreatedAtDesc(projectId.value()).stream()
            .map(this::toDomain)
            .toList();
    }

    @Override
    public CadFile save(CadFile cadFile) {
        CadFileEntity entity = jpaRepository.findById(cadFile.id().value())
            .orElseGet(() -> new CadFileEntity(
                cadFile.id().value(),
                cadFile.projectId().value(),
                cadFile.uploadedBy().value(),
                cadFile.originalFilename(),
                cadFile.contentType(),
                cadFile.fileSizeBytes(),
                cadFile.storagePath(),
                cadFile.status().name()
            ));
        entity.setStatus(cadFile.status().name());
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public void deleteById(CadFileId id) {
        jpaRepository.deleteById(id.value());
    }

    private CadFile toDomain(CadFileEntity e) {
        return new CadFile(
            new CadFileId(e.getId()),
            new ProjectId(e.getProjectId()),
            new UserId(e.getUploadedBy()),
            e.getOriginalFilename(),
            e.getContentType(),
            e.getFileSizeBytes(),
            e.getStoragePath(),
            CadFileStatus.valueOf(e.getStatus()),
            e.getCreatedAt()
        );
    }
}
