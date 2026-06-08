package com.datacenterflow.auth.adapter.in.web.dto;

import com.datacenterflow.auth.domain.port.in.UpdateUserProfileUseCase.UpdateProfileCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request body for updating user profile")
public record UpdateUserProfileRequest(

    @NotBlank
    @Size(max = 100)
    @Schema(description = "Full display name", example = "Jane Smith")
    String fullName,

    @Size(max = 100)
    @Schema(description = "Company or organisation", example = "ACME Data Centers")
    String company
) {

    public UpdateProfileCommand toCommand() {
        return new UpdateProfileCommand(fullName, company);
    }
}
