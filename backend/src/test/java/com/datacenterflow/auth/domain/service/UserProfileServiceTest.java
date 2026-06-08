package com.datacenterflow.auth.domain.service;

import com.datacenterflow.auth.domain.exception.UserNotFoundException;
import com.datacenterflow.auth.domain.model.User;
import com.datacenterflow.auth.domain.model.UserId;
import com.datacenterflow.auth.domain.port.in.UpdateUserProfileUseCase.UpdateProfileCommand;
import com.datacenterflow.auth.domain.port.out.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserProfileService service;

    @BeforeEach
    void setUp() {
        service = new UserProfileService(userRepository);
    }

    @Test
    void getOrCreateProfile_returnsExistingProfile_whenUserExists() {
        UserId userId = new UserId(UUID.randomUUID());
        User existing = user(userId, "alice@example.com");
        given(userRepository.findById(userId)).willReturn(Optional.of(existing));

        User result = service.getOrCreateProfile(userId, "alice@example.com");

        assertThat(result).isEqualTo(existing);
    }

    @Test
    void getOrCreateProfile_createsAndPersistsNewProfile_whenUserNotFound() {
        UserId userId = new UserId(UUID.randomUUID());
        User created = user(userId, "bob@example.com");
        given(userRepository.findById(userId)).willReturn(Optional.empty());
        given(userRepository.save(any())).willReturn(created);

        User result = service.getOrCreateProfile(userId, "bob@example.com");

        assertThat(result.email()).isEqualTo("bob@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateProfile_updatesNameAndCompany_whenUserExists() {
        UserId userId = new UserId(UUID.randomUUID());
        User existing = user(userId, "carol@example.com");
        User updated = existing.withProfile("Carol", "Acme");
        given(userRepository.findById(userId)).willReturn(Optional.of(existing));
        given(userRepository.save(any())).willReturn(updated);

        User result = service.updateProfile(userId, new UpdateProfileCommand("Carol", "Acme"));

        assertThat(result.fullName()).isEqualTo("Carol");
        assertThat(result.company()).isEqualTo("Acme");
    }

    @Test
    void updateProfile_throwsUserNotFoundException_whenUserNotFound() {
        UserId userId = new UserId(UUID.randomUUID());
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateProfile(userId, new UpdateProfileCommand("X", "Y")))
            .isInstanceOf(UserNotFoundException.class)
            .hasMessageContaining(userId.value().toString());
    }

    private User user(UserId id, String email) {
        Instant now = Instant.now();
        return new User(id, email, null, null, "user", now, now);
    }
}
