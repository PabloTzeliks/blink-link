package pablo.tzeliks.blink_link.domain.url.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import pablo.tzeliks.blink_link.domain.url.strategy.ExpirationCalculationStrategy;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the {@link Url} domain model.
 * <p>
 * Covers the {@code isExpired()} method logic with three key scenarios:
 * expired URL, valid URL, and lifetime URL (null expirationDate).
 * Also tests the {@code Url.create()} factory method with an expiration strategy.
 *
 * @author QA Test Suite
 * @since 3.0.0
 */
class UrlDomainTest {

    @Nested
    @DisplayName("isExpired() Tests")
    class IsExpiredTests {

        @Test
        @DisplayName("Should return true when the URL expiration date is in the past")
        void shouldReturnTrueWhenUrlIsExpired() {
            // Arrange: URL that expired yesterday
            LocalDateTime createdAt = LocalDateTime.now().minusDays(10);
            LocalDateTime expirationDate = LocalDateTime.now().minusDays(1);

            Url expiredUrl = Url.restore(1L, UUID.randomUUID(), "https://example.com", "abc123", createdAt, expirationDate);

            // Act
            boolean result = expiredUrl.isExpired();

            // Assert
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false when the URL expiration date is in the future")
        void shouldReturnFalseWhenUrlIsStillValid() {
            // Arrange: URL that expires in 7 days
            LocalDateTime createdAt = LocalDateTime.now();
            LocalDateTime expirationDate = LocalDateTime.now().plusDays(7);

            Url validUrl = Url.restore(2L, UUID.randomUUID(), "https://example.com", "def456", createdAt, expirationDate);

            // Act
            boolean result = validUrl.isExpired();

            // Assert
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false when the URL has no expiration date (lifetime URL)")
        void shouldReturnFalseWhenExpirationDateIsNull() {
            // Arrange: URL with null expiration (lifetime)
            LocalDateTime createdAt = LocalDateTime.now();

            Url lifetimeUrl = Url.restore(3L, UUID.randomUUID(), "https://example.com", "ghi789", createdAt, null);

            // Act
            boolean result = lifetimeUrl.isExpired();

            // Assert
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("Url.create() Factory Method Tests")
    class CreateTests {

        @Test
        @DisplayName("Should create a Url with the expiration date calculated by the strategy")
        void shouldCreateUrlWithExpirationFromStrategy() {
            // Arrange
            ExpirationCalculationStrategy sevenDayStrategy = now -> now.plusDays(7);
            UUID userId = UUID.randomUUID();

            // Act
            Url url = Url.create(100L, userId, "https://example.com", "strat1", sevenDayStrategy);

            // Assert
            assertThat(url.getId()).isEqualTo(100L);
            assertThat(url.getUserId()).isEqualTo(userId);
            assertThat(url.getOriginalUrl()).isEqualTo("https://example.com");
            assertThat(url.getShortCode()).isEqualTo("strat1");
            assertThat(url.getCreatedAt()).isNotNull();
            assertThat(url.getExpirationDate()).isNotNull();
            assertThat(url.getExpirationDate()).isAfter(url.getCreatedAt());
        }
    }
}
