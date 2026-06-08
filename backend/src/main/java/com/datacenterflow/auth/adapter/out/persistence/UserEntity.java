package com.datacenterflow.auth.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_profiles")
class UserEntity {

    @Id
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(name = "company", length = 100)
    private String company;

    @Column(name = "role", nullable = false, length = 20)
    private String role;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected UserEntity() {}

    UserEntity(UUID id, String email, String fullName, String company, String role) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.company = company;
        this.role = role;
    }

    UUID getId() { return id; }
    String getEmail() { return email; }
    String getFullName() { return fullName; }
    String getCompany() { return company; }
    String getRole() { return role; }
    Instant getCreatedAt() { return createdAt; }
    Instant getUpdatedAt() { return updatedAt; }

    void setFullName(String fullName) { this.fullName = fullName; }
    void setCompany(String company) { this.company = company; }
}
