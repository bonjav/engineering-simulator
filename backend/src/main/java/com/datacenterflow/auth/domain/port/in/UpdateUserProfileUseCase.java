package com.datacenterflow.auth.domain.port.in;

import com.datacenterflow.auth.domain.model.User;
import com.datacenterflow.auth.domain.model.UserId;

public interface UpdateUserProfileUseCase {

    User updateProfile(UserId userId, UpdateProfileCommand command);

    record UpdateProfileCommand(String fullName, String company) {}
}
