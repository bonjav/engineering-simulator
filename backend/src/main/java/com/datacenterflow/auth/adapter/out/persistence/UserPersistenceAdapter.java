package com.datacenterflow.auth.adapter.out.persistence;

import com.datacenterflow.auth.domain.model.User;
import com.datacenterflow.auth.domain.model.UserId;
import com.datacenterflow.auth.domain.port.out.UserRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
class UserPersistenceAdapter implements UserRepository {

    private final UserJpaRepository jpaRepository;

    UserPersistenceAdapter(UserJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<User> findById(UserId userId) {
        return jpaRepository.findById(userId.value()).map(this::toDomain);
    }

    @Override
    public User save(User user) {
        UserEntity entity = jpaRepository.findById(user.id().value())
            .orElseGet(() -> new UserEntity(user.id().value(), user.email(), user.fullName(), user.company(), user.role()));

        entity.setFullName(user.fullName());
        entity.setCompany(user.company());

        return toDomain(jpaRepository.save(entity));
    }

    private User toDomain(UserEntity e) {
        return new User(
            new UserId(e.getId()),
            e.getEmail(),
            e.getFullName(),
            e.getCompany(),
            e.getRole(),
            e.getCreatedAt(),
            e.getUpdatedAt()
        );
    }
}
