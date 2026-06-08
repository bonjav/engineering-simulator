package com.datacenterflow.auth.adapter.in.web;

import com.datacenterflow.auth.adapter.in.web.dto.UpdateUserProfileRequest;
import com.datacenterflow.auth.adapter.in.web.dto.UserProfileResponse;
import com.datacenterflow.auth.domain.model.UserId;
import com.datacenterflow.auth.domain.port.in.GetUserProfileUseCase;
import com.datacenterflow.auth.domain.port.in.UpdateUserProfileUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User Profile", description = "Authenticated user profile management")
@SecurityRequirement(name = "bearerAuth")
public class UserProfileController {

    private final GetUserProfileUseCase getUserProfileUseCase;
    private final UpdateUserProfileUseCase updateUserProfileUseCase;

    public UserProfileController(
        GetUserProfileUseCase getUserProfileUseCase,
        UpdateUserProfileUseCase updateUserProfileUseCase
    ) {
        this.getUserProfileUseCase = getUserProfileUseCase;
        this.updateUserProfileUseCase = updateUserProfileUseCase;
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user profile", description = "Returns the profile of the authenticated user, creating it on first call.")
    public ResponseEntity<UserProfileResponse> getMyProfile(JwtAuthenticationToken auth) {
        UserId userId = UserId.of(auth.getName());
        String email = auth.getToken().getClaimAsString("email");
        return ResponseEntity.ok(UserProfileResponse.from(
            getUserProfileUseCase.getOrCreateProfile(userId, email)
        ));
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user profile")
    public ResponseEntity<UserProfileResponse> updateMyProfile(
        JwtAuthenticationToken auth,
        @Valid @RequestBody UpdateUserProfileRequest request
    ) {
        UserId userId = UserId.of(auth.getName());
        return ResponseEntity.ok(UserProfileResponse.from(
            updateUserProfileUseCase.updateProfile(userId, request.toCommand())
        ));
    }
}
