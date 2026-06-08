package com.datacenterflow.auth.adapter.out.persistence;

import com.datacenterflow.auth.domain.model.User;
import com.datacenterflow.auth.domain.model.UserId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@Import(UserPersistenceAdapter.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserPersistenceAdapterTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("dcf_test")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void overrideDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Autowired
    UserPersistenceAdapter adapter;

    @Test
    void save_persistsNewUser_andCanBeRetrievedById() {
        UserId userId = new UserId(UUID.randomUUID());
        User user = new User(userId, "test@example.com", null, null, "user", Instant.now(), Instant.now());

        User saved = adapter.save(user);
        Optional<User> found = adapter.findById(userId);

        assertThat(found).isPresent();
        assertThat(found.get().email()).isEqualTo("test@example.com");
        assertThat(found.get().id()).isEqualTo(userId);
        assertThat(saved.createdAt()).isNotNull();
    }

    @Test
    void save_updatesExistingUser_whenCalledTwice() {
        UserId userId = new UserId(UUID.randomUUID());
        User user = new User(userId, "update@example.com", null, null, "user", Instant.now(), Instant.now());
        adapter.save(user);

        User updated = user.withProfile("New Name", "New Corp");
        adapter.save(updated);

        Optional<User> found = adapter.findById(userId);
        assertThat(found).isPresent();
        assertThat(found.get().fullName()).isEqualTo("New Name");
        assertThat(found.get().company()).isEqualTo("New Corp");
    }

    @Test
    void findById_returnsEmpty_whenUserDoesNotExist() {
        Optional<User> result = adapter.findById(new UserId(UUID.randomUUID()));
        assertThat(result).isEmpty();
    }
}
