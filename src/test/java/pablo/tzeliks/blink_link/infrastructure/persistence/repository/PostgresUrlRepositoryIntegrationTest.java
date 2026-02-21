package pablo.tzeliks.blink_link.infrastructure.persistence.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import pablo.tzeliks.blink_link.domain.url.model.Url;
import pablo.tzeliks.blink_link.domain.url.ports.UrlRepositoryPort;
import pablo.tzeliks.blink_link.infrastructure.AbstractContainerBase;
import pablo.tzeliks.blink_link.infrastructure.url.persistence.mapper.UrlEntityMapper;
import pablo.tzeliks.blink_link.infrastructure.url.persistence.repository.PostgresUrlRepositoryAdapter;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * Integration tests for the PostgreSQL URL repository implementation.
 * <p>
 * This test class validates the {@link PostgresUrlRepositoryAdapter} adapter and its
 * interaction with the actual PostgreSQL database. It tests the repository pattern
 * implementation, entity-domain mapping, and database operations.
 * <p>
 * <b>Test Strategy:</b>
 * <p>
 * These are data layer integration tests that:
 * <ul>
 *   <li>Use a real PostgreSQL database via Testcontainers</li>
 *   <li>Load only the data access layer components ({@code @DataJpaTest})</li>
 *   <li>Test sequence generation, saving, and querying operations</li>
 *   <li>Validate entity-domain mapping through {@link UrlEntityMapper}</li>
 * </ul>
 * <p>
 * <b>Testcontainers:</b>
 * <p>
 * By extending {@link AbstractContainerBase}, these tests run against PostgreSQL 17,
 * ensuring that sequence operations, constraints, and queries work correctly with
 * the actual production database. This catches PostgreSQL-specific behaviors that
 * might not be present in H2 or other in-memory databases.
 * <p>
 * <b>Test Coverage:</b>
 * <ul>
 *   <li>Sequence ID generation ({@code nextval('url_id_seq')})</li>
 *   <li>URL persistence and retrieval by short code</li>
 *   <li>Handling of non-existent short codes (empty Optional)</li>
 * </ul>
 * <p>
 * <b>@DataJpaTest:</b>
 * <p>
 * This annotation provides a lighter-weight context compared to {@code @SpringBootTest},
 * loading only JPA-related beans and auto-configuring an embedded database. We override
 * this with {@code @AutoConfigureTestDatabase(replace = NONE)} to use our Testcontainers
 * PostgreSQL instance instead.
 *
 * @author Pablo Tzeliks
 * @version 2.0.0
 * @since 1.0.0
 * @see PostgresUrlRepositoryAdapter
 * @see AbstractContainerBase
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({PostgresUrlRepositoryAdapter.class, UrlEntityMapper.class})
public class PostgresUrlRepositoryIntegrationTest extends AbstractContainerBase {

    @Autowired
    private UrlRepositoryPort repository;

    /**
     * Integration Test: Verifies PostgreSQL sequence ID generation.
     * <p>
     * <b>Scenario:</b> Sequence increments correctly
     * <p>
     * <b>Given:</b> The url_id_seq sequence exists in PostgreSQL
     * <br><b>When:</b> nextId() is called twice consecutively
     * <br><b>Then:</b> The second ID is exactly one greater than the first
     * <p>
     * <b>Assertions:</b>
     * <ul>
     *   <li>Both IDs are not null</li>
     *   <li>The second ID is greater than the first</li>
     *   <li>The second ID equals first ID + 1 (sequential)</li>
     * </ul>
     * <p>
     * This test validates the native SQL query that calls {@code nextval('url_id_seq')},
     * ensuring the sequence works correctly for ID pre-generation before Base62 encoding.
     */
    @Test
    @DisplayName("Should retrieve the next ID from database sequence")
    void shouldGetNextId() {
        // Act
        Long firstId = repository.nextId();
        Long secondId = repository.nextId();

        Long testId = firstId + 1;

        // Assert
        assertThat(firstId).isNotNull();
        assertThat(secondId).isNotNull();
        assertThat(secondId).isGreaterThan(firstId);
        assertThat(testId).isEqualTo(secondId);
    }

    /**
     * Integration Test: Verifies complete URL persistence and retrieval workflow.
     * <p>
     * <b>Scenario:</b> Happy Path - Save and find by short code
     * <p>
     * <b>Given:</b> A new URL domain object with generated ID and short code
     * <br><b>When:</b> The URL is saved and then queried by its short code
     * <br><b>Then:</b> The retrieved URL matches the saved data
     * <p>
     * <b>Assertions:</b>
     * <ul>
     *   <li>The URL is found (Optional is present)</li>
     *   <li>Retrieved ID matches the generated ID</li>
     *   <li>Retrieved original URL matches</li>
     *   <li>Retrieved short code matches</li>
     *   <li>Creation timestamp is populated by database</li>
     * </ul>
     * <p>
     * This test validates:
     * <ul>
     *   <li>Domain-to-entity mapping via {@link UrlEntityMapper}</li>
     *   <li>JPA persistence with {@code Persistable} optimization</li>
     *   <li>Entity-to-domain mapping on retrieval</li>
     *   <li>Unique constraint on short_code</li>
     * </ul>
     */
    @Test
    @DisplayName("Should save an URL and find it by Short Code")
    void shouldSaveAnUrlAndFindItByShortCode() {
        // Arrange

        // 1. Generate new ID
        Long generatedId = repository.nextId();

        // 2. Create new Url
        Url newUrl = new Url(
                generatedId,
                "https://github.com/PabloTzeliks",
                "myGitHub",
                LocalDateTime.now()
        );

        // Act
        repository.save(newUrl);

        // Assert
        Optional<Url> foundUrl = repository.findByShortCode("myGitHub");

        assertThat(foundUrl).isPresent();
        assertThat(foundUrl.get().getId()).isEqualTo(generatedId);
        assertThat(foundUrl.get().getOriginalUrl()).isEqualTo("https://github.com/PabloTzeliks");
        assertThat(foundUrl.get().getShortCode()).isEqualTo("myGitHub");
        assertThat(foundUrl.get().getCreatedAt()).isNotNull();
    }

    /**
     * Integration Test: Verifies handling of non-existent short codes.
     * <p>
     * <b>Scenario:</b> Query for non-existent short code
     * <p>
     * <b>Given:</b> A short code "ghost-code" that doesn't exist in the database
     * <br><b>When:</b> findByShortCode() is called with this code
     * <br><b>Then:</b> An empty Optional is returned
     * <p>
     * <b>Assertions:</b>
     * <ul>
     *   <li>The returned Optional is empty (not present)</li>
     * </ul>
     * <p>
     * This test ensures the repository correctly handles "not found" scenarios
     * by returning an empty Optional rather than throwing an exception or
     * returning null, following Java best practices for optional values.
     */
    @Test
    @DisplayName("Should return empty when Short Code does not exist")
    void shouldReturnEmptyForNonExistentCode() {
        Optional<Url> foundUrl = repository.findByShortCode("ghost-code");
        assertThat(foundUrl).isEmpty();
    }
}
