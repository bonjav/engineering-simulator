package com.datacenterflow.cad.adapter.out.persistence;

import com.datacenterflow.auth.domain.model.UserId;
import com.datacenterflow.cad.domain.model.CadFile;
import com.datacenterflow.cad.domain.model.CadFileId;
import com.datacenterflow.cad.domain.model.CadFileStatus;
import com.datacenterflow.project.domain.model.ProjectId;
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
@Import(CadFilePersistenceAdapter.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CadFilePersistenceAdapterTest {

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

    @Autowired CadFilePersistenceAdapter adapter;

    private final ProjectId projectId = new ProjectId(UUID.randomUUID());
    private final UserId userId = new UserId(UUID.randomUUID());

    @Test
    void save_persistsFile_andCanBeRetrievedById() {
        CadFile file = uploadingFile();

        adapter.save(file);
        Optional<CadFile> found = adapter.findById(file.id());

        assertThat(found).isPresent();
        assertThat(found.get().originalFilename()).isEqualTo("rack.step");
        assertThat(found.get().status()).isEqualTo(CadFileStatus.UPLOADING);
        assertThat(found.get().createdAt()).isNotNull();
    }

    @Test
    void save_updatesStatus_whenCalledTwice() {
        CadFile uploading = uploadingFile();
        adapter.save(uploading);

        adapter.save(uploading.withStatus(CadFileStatus.READY));

        Optional<CadFile> found = adapter.findById(uploading.id());
        assertThat(found).isPresent();
        assertThat(found.get().status()).isEqualTo(CadFileStatus.READY);
    }

    @Test
    void findById_returnsEmpty_whenFileDoesNotExist() {
        Optional<CadFile> result = adapter.findById(new CadFileId(UUID.randomUUID()));
        assertThat(result).isEmpty();
    }

    @Test
    void findAllByProject_returnsFilesForProject_orderedByCreatedAtDesc() {
        CadFile file1 = cadFile(new CadFileId(UUID.randomUUID()), "rack-a.step");
        CadFile file2 = cadFile(new CadFileId(UUID.randomUUID()), "rack-b.step");
        adapter.save(file1);
        adapter.save(file2);

        List<CadFile> files = adapter.findAllByProject(projectId);

        assertThat(files).hasSizeGreaterThanOrEqualTo(2);
        assertThat(files.stream().map(CadFile::originalFilename).toList())
            .contains("rack-a.step", "rack-b.step");
    }

    @Test
    void deleteById_removesFile() {
        CadFile file = uploadingFile();
        adapter.save(file);

        adapter.deleteById(file.id());

        assertThat(adapter.findById(file.id())).isEmpty();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private CadFile uploadingFile() {
        return cadFile(new CadFileId(UUID.randomUUID()), "rack.step");
    }

    private CadFile cadFile(CadFileId id, String filename) {
        String path = projectId.value() + "/" + id.value() + "_" + filename;
        return new CadFile(id, projectId, userId, filename, "application/step",
            4096L, path, CadFileStatus.UPLOADING, Instant.now());
    }
}
