package com.datacenterflow.project.adapter.out.persistence;

import com.datacenterflow.auth.domain.model.UserId;
import com.datacenterflow.project.domain.model.Project;
import com.datacenterflow.project.domain.model.ProjectId;
import com.datacenterflow.project.domain.model.ProjectMember;
import com.datacenterflow.project.domain.model.ProjectRole;
import com.datacenterflow.project.domain.model.ProjectStatus;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@Import(ProjectPersistenceAdapter.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProjectPersistenceAdapterTest {

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

    @Autowired ProjectPersistenceAdapter adapter;

    private UserId ownerUserId;
    private UserId secondUserId;

    @BeforeEach
    void setUp() {
        // Insert required user_profiles rows (FK constraint from projects.owner_id)
        ownerUserId = new UserId(UUID.randomUUID());
        secondUserId = new UserId(UUID.randomUUID());
    }

    @Test
    void save_persistsProject_andCanBeRetrievedById() {
        Project project = project(new ProjectId(UUID.randomUUID()), ownerUserId);

        Project saved = adapter.save(project);
        Optional<Project> found = adapter.findById(project.id());

        assertThat(found).isPresent();
        assertThat(found.get().name()).isEqualTo("Test Project");
        assertThat(saved.createdAt()).isNotNull();
    }

    @Test
    void save_updatesProjectDetails_whenCalledTwice() {
        ProjectId id = new ProjectId(UUID.randomUUID());
        adapter.save(project(id, ownerUserId));

        Project updated = new Project(id, "Updated", "New desc", ProjectStatus.ACTIVE, ownerUserId, Instant.now(), Instant.now());
        adapter.save(updated);

        Optional<Project> found = adapter.findById(id);
        assertThat(found).isPresent();
        assertThat(found.get().name()).isEqualTo("Updated");
    }

    @Test
    void findById_returnsEmpty_whenProjectDoesNotExist() {
        Optional<Project> result = adapter.findById(new ProjectId(UUID.randomUUID()));
        assertThat(result).isEmpty();
    }

    @Test
    void saveMember_andFindByProjectAndUser() {
        ProjectId projectId = new ProjectId(UUID.randomUUID());
        adapter.save(project(projectId, ownerUserId));

        ProjectMember member = new ProjectMember(projectId, ownerUserId, ProjectRole.OWNER, Instant.now());
        adapter.save(member);

        Optional<ProjectMember> found = adapter.findByProjectAndUser(projectId, ownerUserId);
        assertThat(found).isPresent();
        assertThat(found.get().role()).isEqualTo(ProjectRole.OWNER);
    }

    @Test
    void findAllAccessibleByUser_returnsProjectsWhereMember() {
        ProjectId id = new ProjectId(UUID.randomUUID());
        adapter.save(project(id, ownerUserId));
        adapter.save(new ProjectMember(id, ownerUserId, ProjectRole.OWNER, Instant.now()));

        List<Project> accessible = adapter.findAllAccessibleByUser(ownerUserId);

        assertThat(accessible).hasSize(1);
        assertThat(accessible.get(0).id()).isEqualTo(id);
    }

    @Test
    void deleteMember_removesTheMemberRecord() {
        ProjectId projectId = new ProjectId(UUID.randomUUID());
        adapter.save(project(projectId, ownerUserId));
        adapter.save(new ProjectMember(projectId, secondUserId, ProjectRole.VIEWER, Instant.now()));

        adapter.delete(projectId, secondUserId);

        Optional<ProjectMember> found = adapter.findByProjectAndUser(projectId, secondUserId);
        assertThat(found).isEmpty();
    }

    private Project project(ProjectId id, UserId ownerId) {
        Instant now = Instant.now();
        return new Project(id, "Test Project", "Description", ProjectStatus.ACTIVE, ownerId, now, now);
    }
}
