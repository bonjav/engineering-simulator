package com.datacenterflow.auth.adapter.in.web.dto;

import com.datacenterflow.auth.domain.model.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "User profile data")
public record UserProfileResponse(

    @Schema(description = "Supabase user UUID", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    String id,

    @Schema(description = "User email address", example = "engineer@acme.com")
    String email,

    @Schema(description = "Full display name", example = "Jane Smith")
    String fullName,

    @Schema(description = "Company or organisation", example = "ACME Data Centers")
    String company,

    @Schema(description = "Platform role", example = "user")
    String role,

    @Schema(description = "Profile creation timestamp")
    Instant createdAt,

    @Schema(description = "Profile last-updated timestamp")
    Instant updatedAt
) {

    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
            user.id().value().toString(),
            user.email(),
            user.fullName(),
            user.company(),
            user.role(),
            user.createdAt(),
            user.updatedAt()
        );
    }
}
