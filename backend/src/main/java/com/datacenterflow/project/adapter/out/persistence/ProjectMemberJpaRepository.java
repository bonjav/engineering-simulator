package com.datacenterflow.project.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface ProjectMemberJpaRepository extends JpaRepository<ProjectMemberEntity, Long> {

    Optional<ProjectMemberEntity> findByProjectIdAndUserId(UUID projectId, UUID userId);

    List<ProjectMemberEntity> findAllByProjectId(UUID projectId);

    @Modifying
    @Query("DELETE FROM ProjectMemberEntity pm WHERE pm.projectId = :projectId AND pm.userId = :userId")
    void deleteByProjectIdAndUserId(@Param("projectId") UUID projectId, @Param("userId") UUID userId);
}
