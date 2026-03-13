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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the {@link PostgresUrlRepositoryAdapter#deleteExpiredInBatch} method.
 * <p>
 * Uses a real PostgreSQL database via Testcontainers to validate
 * the native DELETE query with LIMIT and FOR UPDATE SKIP LOCKED.
 *
 * @since 3.0.0
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({PostgresUrlRepositoryAdapter.class, UrlEntityMapper.class})
public class DeleteExpiredInBatchIntegrationTest extends AbstractContainerBase {

    @Autowired
    private UrlRepositoryPort repository;

    @Test
    @DisplayName("Should delete only expired URLs and respect batch size")
    void shouldDeleteOnlyExpiredUrlsRespectingBatchSize() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime pastExpiration = now.minusDays(1);
        LocalDateTime futureExpiration = now.plusDays(7);

        // Insert 3 expired URLs
        for (int i = 0; i < 3; i++) {
            Long id = repository.nextId();
            Url expired = Url.restore(id, "https://expired-" + i + ".com", "exp" + id, now.minusDays(10), pastExpiration);
            repository.save(expired);
        }

        // Insert 2 non-expired URLs
        for (int i = 0; i < 2; i++) {
            Long id = repository.nextId();
            Url valid = Url.restore(id, "https://valid-" + i + ".com", "val" + id, now, futureExpiration);
            repository.save(valid);
        }

        // Act - Delete expired with batch size of 10
        int deleted = repository.deleteExpiredInBatch(now, 10);

        // Assert - Only the 3 expired URLs should be deleted
        assertThat(deleted).isEqualTo(3);
    }

    @Test
    @DisplayName("Should respect batch size limit when more expired URLs exist")
    void shouldRespectBatchSizeLimit() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime pastExpiration = now.minusDays(1);

        // Insert 5 expired URLs
        for (int i = 0; i < 5; i++) {
            Long id = repository.nextId();
            Url expired = Url.restore(id, "https://batch-" + i + ".com", "bat" + id, now.minusDays(10), pastExpiration);
            repository.save(expired);
        }

        // Act - Delete with batch size of 2 (less than total expired)
        int deleted = repository.deleteExpiredInBatch(now, 2);

        // Assert - Only 2 should be deleted (batch limit)
        assertThat(deleted).isEqualTo(2);
    }

    @Test
    @DisplayName("Should return zero when no expired URLs exist")
    void shouldReturnZeroWhenNoExpiredUrls() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime futureExpiration = now.plusDays(7);

        Long id = repository.nextId();
        Url valid = Url.restore(id, "https://valid.com", "val" + id, now, futureExpiration);
        repository.save(valid);

        // Act
        int deleted = repository.deleteExpiredInBatch(now, 100);

        // Assert
        assertThat(deleted).isEqualTo(0);
    }

    @Test
    @DisplayName("Should not delete non-expired URLs")
    void shouldNotDeleteNonExpiredUrls() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime pastExpiration = now.minusDays(1);
        LocalDateTime futureExpiration = now.plusDays(7);

        // Insert 1 expired URL
        Long expiredId = repository.nextId();
        Url expired = Url.restore(expiredId, "https://expired.com", "e" + expiredId, now.minusDays(10), pastExpiration);
        repository.save(expired);

        // Insert 1 non-expired URL
        Long validId = repository.nextId();
        String validShortCode = "v" + validId;
        Url valid = Url.restore(validId, "https://valid.com", validShortCode, now, futureExpiration);
        repository.save(valid);

        // Act
        int deleted = repository.deleteExpiredInBatch(now, 100);

        // Assert - Only expired URL should be deleted
        assertThat(deleted).isEqualTo(1);

        // Non-expired URL should still exist
        Optional<Url> found = repository.findByShortCode(validShortCode);
        assertThat(found).isPresent();
        assertThat(found.get().getOriginalUrl()).isEqualTo("https://valid.com");
    }
}
