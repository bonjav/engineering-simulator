package com.datacenterflow.auth.domain.model;

import java.time.Instant;

public record User(
    UserId id,
    String email,
    String fullName,
    String company,
    String role,
    Instant createdAt,
    Instant updatedAt
) {

    public User withProfile(String fullName, String company) {
        return new User(id, email, fullName, company, role, createdAt, Instant.now());
    }
}
