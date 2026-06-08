package com.datacenterflow.project.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

interface ProjectJpaRepository extends JpaRepository<ProjectEntity, UUID> {

    @Query("""
        SELECT p FROM ProjectEntity p
        WHERE p.id IN (
            SELECT pm.projectId FROM ProjectMemberEntity pm WHERE pm.userId = :userId
        )
        ORDER BY p.createdAt DESC
        """)
    List<ProjectEntity> findAllAccessibleByUser(@Param("userId") UUID userId);
}
