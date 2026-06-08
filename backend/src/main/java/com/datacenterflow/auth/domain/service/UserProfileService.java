package com.datacenterflow.auth.domain.service;

import com.datacenterflow.auth.domain.exception.UserNotFoundException;
import com.datacenterflow.auth.domain.model.User;
import com.datacenterflow.auth.domain.model.UserId;
import com.datacenterflow.auth.domain.port.in.GetUserProfileUseCase;
import com.datacenterflow.auth.domain.port.in.UpdateUserProfileUseCase;
import com.datacenterflow.auth.domain.port.out.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
public class UserProfileService implements GetUserProfileUseCase, UpdateUserProfileUseCase {

    private final UserRepository userRepository;

    public UserProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User getOrCreateProfile(UserId userId, String email) {
        return userRepository.findById(userId)
            .orElseGet(() -> userRepository.save(
                new User(userId, email, null, null, "user", Instant.now(), Instant.now())
            ));
    }

    @Override
    public User updateProfile(UserId userId, UpdateProfileCommand command) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
        return userRepository.save(user.withProfile(command.fullName(), command.company()));
    }
}
