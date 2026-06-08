package com.datacenterflow.auth.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {}
