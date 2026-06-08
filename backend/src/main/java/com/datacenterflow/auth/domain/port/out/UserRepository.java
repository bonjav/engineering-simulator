package com.datacenterflow.auth.domain.port.out;

import com.datacenterflow.auth.domain.model.User;
import com.datacenterflow.auth.domain.model.UserId;

import java.util.Optional;

public interface UserRepository {

    Optional<User> findById(UserId userId);

    User save(User user);
}
