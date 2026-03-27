package pablo.tzeliks.blink_link.infrastructure.persistence.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import pablo.tzeliks.blink_link.domain.url.model.Url;
import pablo.tzeliks.blink_link.domain.url.ports.UrlRepositoryPort;
import pablo.tzeliks.blink_link.domain.user.model.AuthProvider;
import pablo.tzeliks.blink_link.domain.user.model.Plan;
import pablo.tzeliks.blink_link.domain.user.model.Role;
import pablo.tzeliks.blink_link.infrastructure.AbstractContainerBase;
import pablo.tzeliks.blink_link.infrastructure.url.persistence.mapper.UrlEntityMapper;
import pablo.tzeliks.blink_link.infrastructure.url.persistence.repository.PostgresUrlRepositoryAdapter;
import pablo.tzeliks.blink_link.infrastructure.user.persistence.entity.UserEntity;
import pablo.tzeliks.blink_link.infrastructure.user.persistence.repository.JpaUserRepository;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the {@code deleteExpiredInBatch} method
 * in {@link PostgresUrlRepositoryAdapter}.
 * <p>
 * Uses {@code @DataJpaTest} with Testcontainers PostgreSQL to validate
 * that the native DELETE query with LIMIT and FOR UPDATE SKIP LOCKED
 * correctly deletes only expired URLs and respects the batch size.
 *
 * @author QA Test Suite
 * @since 3.0.0
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({PostgresUrlRepositoryAdapter.class, UrlEntityMapper.class})
class DeleteExpiredInBatchIntegrationTest extends AbstractContainerBase {

    @Autowired
    private UrlRepositoryPort repository;

    @Autowired
    private JpaUserRepository jpaUserRepository;

    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        UserEntity userEntity = new UserEntity(testUserId, "batch-test@example.com", "encoded",
                Role.USER, Plan.FREE, AuthProvider.LOCAL, LocalDateTime.now(), LocalDateTime.now());
        jpaUserRepository.saveAndFlush(userEntity);
    }

    @Test
    @DisplayName("Should delete only expired URLs and respect the batch size limit")
    void shouldDeleteOnlyExpiredUrlsRespectingBatchSize() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime pastDate = now.minusDays(10);
        LocalDateTime futureDate = now.plusDays(10);

        // Insert 3 expired URLs
        for (int i = 0; i < 3; i++) {
            Long id = repository.nextId();
            Url expired = Url.restore(id, testUserId, "https://expired-" + i + ".com", "exp" + i, now.minusDays(20), pastDate);
            repository.save(expired);
        }

        // Insert 2 valid (non-expired) URLs
        for (int i = 0; i < 2; i++) {
            Long id = repository.nextId();
            Url valid = Url.restore(id, testUserId, "https://valid-" + i + ".com", "val" + i, now, futureDate);
            repository.save(valid);
        }

        // Act: Delete expired URLs with batch size of 10 (larger than count)
        int deleted = repository.deleteExpiredInBatch(now, 10);

        // Assert: Only 3 expired URLs should be deleted
        assertThat(deleted).isEqualTo(3);
    }

    @Test
    @DisplayName("Should respect the batch size limit when more expired URLs exist")
    void shouldRespectBatchSizeLimit() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime pastDate = now.minusDays(5);

        // Insert 5 expired URLs
        for (int i = 0; i < 5; i++) {
            Long id = repository.nextId();
            Url expired = Url.restore(id, testUserId, "https://batch-" + i + ".com", "btc" + i, now.minusDays(10), pastDate);
            repository.save(expired);
        }

        // Act: Delete with batch size of 2 (smaller than total expired count)
        int deleted = repository.deleteExpiredInBatch(now, 2);

        // Assert: Only 2 should be deleted (respecting batch size)
        assertThat(deleted).isEqualTo(2);
    }

    @Test
    @DisplayName("Should return zero when no expired URLs exist")
    void shouldReturnZeroWhenNoExpiredUrls() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime futureDate = now.plusDays(30);

        Long id = repository.nextId();
        Url valid = Url.restore(id, testUserId, "https://valid.com", "noExp", now, futureDate);
        repository.save(valid);

        // Act
        int deleted = repository.deleteExpiredInBatch(now, 100);

        // Assert
        assertThat(deleted).isZero();
    }

    @Test
    @DisplayName("Should not delete non-expired URLs when deleting expired ones")
    void shouldNotDeleteNonExpiredUrls() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime pastDate = now.minusDays(1);
        LocalDateTime futureDate = now.plusDays(30);

        // Insert 1 expired URL
        Long expiredId = repository.nextId();
        Url expired = Url.restore(expiredId, testUserId, "https://expired.com", "expd", now.minusDays(10), pastDate);
        repository.save(expired);

        // Insert 1 valid URL
        Long validId = repository.nextId();
        String validShortCode = "valid";
        Url valid = Url.restore(validId, testUserId, "https://valid.com", validShortCode, now, futureDate);
        repository.save(valid);

        // Act
        repository.deleteExpiredInBatchReturningCodes(now, 100);

        // Assert: The valid URL should still exist
        assertThat(repository.findByShortCode(validShortCode)).isPresent();
    }
}
