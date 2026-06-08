package com.datacenterflow.cad.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface CadFileJpaRepository extends JpaRepository<CadFileEntity, UUID> {

    List<CadFileEntity> findAllByProjectIdOrderByCreatedAtDesc(UUID projectId);
}
