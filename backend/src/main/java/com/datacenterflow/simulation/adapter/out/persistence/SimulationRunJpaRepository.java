package com.datacenterflow.simulation.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface SimulationRunJpaRepository extends JpaRepository<SimulationRunEntity, UUID> {

    List<SimulationRunEntity> findAllByProjectIdOrderByStartedAtDesc(UUID projectId);
}
