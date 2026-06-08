package com.datacenterflow.auth.domain.exception;

import com.datacenterflow.auth.domain.model.UserId;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(UserId userId) {
        super("User profile not found: " + userId.value());
    }
}
