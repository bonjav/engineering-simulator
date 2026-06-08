package com.datacenterflow.cad.domain.port.out;

import com.datacenterflow.cad.domain.model.CadFile;
import com.datacenterflow.cad.domain.model.CadFileId;
import com.datacenterflow.project.domain.model.ProjectId;

import java.util.List;
import java.util.Optional;

public interface CadFileRepository {

    Optional<CadFile> findById(CadFileId id);

    List<CadFile> findAllByProject(ProjectId projectId);

    CadFile save(CadFile cadFile);

    void deleteById(CadFileId id);
}
