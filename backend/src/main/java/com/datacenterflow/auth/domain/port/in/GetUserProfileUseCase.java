package com.datacenterflow.auth.domain.port.in;

import com.datacenterflow.auth.domain.model.User;
import com.datacenterflow.auth.domain.model.UserId;

public interface GetUserProfileUseCase {

    /**
     * Returns the user's profile, creating it on first access if it does not exist.
     * The email is extracted from the JWT claim and used only during creation.
     */
    User getOrCreateProfile(UserId userId, String email);
}
