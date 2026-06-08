package com.datacenterflow.auth.adapter.in.web;

import com.datacenterflow.auth.domain.model.User;
import com.datacenterflow.auth.domain.model.UserId;
import com.datacenterflow.auth.domain.port.in.GetUserProfileUseCase;
import com.datacenterflow.auth.domain.port.in.UpdateUserProfileUseCase;
import com.datacenterflow.infrastructure.exception.GlobalExceptionHandler;
import com.datacenterflow.infrastructure.security.SecurityConfig;
import com.datacenterflow.infrastructure.security.SupabaseProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserProfileController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class UserProfileControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    GetUserProfileUseCase getUserProfileUseCase;

    @MockBean
    UpdateUserProfileUseCase updateUserProfileUseCase;

    // Mocked so SecurityConfig can be imported without a real Supabase secret
    @MockBean
    JwtDecoder jwtDecoder;

    @MockBean
    SupabaseProperties supabaseProperties;

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String EMAIL = "engineer@acme.com";

    @Test
    void getMyProfile_returns200_withUserProfile() throws Exception {
        given(getUserProfileUseCase.getOrCreateProfile(any(), any())).willReturn(testUser());

        mockMvc.perform(get("/api/v1/users/me")
                .with(jwt().jwt(j -> j.subject(USER_ID.toString()).claim("email", EMAIL))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(USER_ID.toString()))
            .andExpect(jsonPath("$.email").value(EMAIL))
            .andExpect(jsonPath("$.role").value("user"));
    }

    @Test
    void getMyProfile_returns401_whenNoToken() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void updateMyProfile_returns200_withUpdatedProfile() throws Exception {
        User updated = new User(new UserId(USER_ID), EMAIL, "Jane Smith", "ACME", "user", Instant.now(), Instant.now());
        given(updateUserProfileUseCase.updateProfile(any(), any())).willReturn(updated);

        String body = objectMapper.writeValueAsString(Map.of("fullName", "Jane Smith", "company", "ACME"));

        mockMvc.perform(put("/api/v1/users/me")
                .with(jwt().jwt(j -> j.subject(USER_ID.toString()).claim("email", EMAIL)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.fullName").value("Jane Smith"))
            .andExpect(jsonPath("$.company").value("ACME"));
    }

    @Test
    void updateMyProfile_returns400_whenFullNameIsBlank() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("fullName", ""));

        mockMvc.perform(put("/api/v1/users/me")
                .with(jwt().jwt(j -> j.subject(USER_ID.toString())))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest());
    }

    private User testUser() {
        Instant now = Instant.now();
        return new User(new UserId(USER_ID), EMAIL, null, null, "user", now, now);
    }
}
