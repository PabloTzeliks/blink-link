package pablo.tzeliks.blink_link.domain.url.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import pablo.tzeliks.blink_link.domain.common.exception.DomainException;
import pablo.tzeliks.blink_link.domain.url.exception.InvalidUrlException;
import pablo.tzeliks.blink_link.domain.url.strategy.ExpirationCalculationStrategy;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link Url} domain model.
 * <p>
 * Tests cover creation, restoration, validation, and the expiration logic
 * introduced by the TTL feature.
 *
 * @since 3.0.0
 */
class UrlTest {

    private static final Long VALID_ID = 1000000L;
    private static final String VALID_ORIGINAL_URL = "https://github.com/PabloTzeliks";
    private static final String VALID_SHORT_CODE = "HhqS";

    @Nested
    @DisplayName("Url.restore() tests")
    class RestoreTests {

        @Test
        @DisplayName("Should restore a valid Url with expirationDate")
        void shouldRestoreValidUrl() {
            // Arrange
            LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 12, 0);
            LocalDateTime expirationDate = createdAt.plusDays(7);

            // Act
            Url url = Url.restore(VALID_ID, VALID_ORIGINAL_URL, VALID_SHORT_CODE, createdAt, expirationDate);

            // Assert
            assertEquals(VALID_ID, url.getId());
            assertEquals(VALID_ORIGINAL_URL, url.getOriginalUrl());
            assertEquals(VALID_SHORT_CODE, url.getShortCode());
            assertEquals(createdAt, url.getCreatedAt());
            assertEquals(expirationDate, url.getExpirationDate());
        }

        @Test
        @DisplayName("Should restore a lifetime Url with null expirationDate")
        void shouldRestoreLifetimeUrl() {
            // Arrange
            LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 12, 0);

            // Act
            Url url = Url.restore(VALID_ID, VALID_ORIGINAL_URL, VALID_SHORT_CODE, createdAt, null);

            // Assert
            assertNull(url.getExpirationDate());
        }

        @Test
        @DisplayName("Should throw InvalidUrlException when originalUrl is null")
        void shouldThrowWhenOriginalUrlIsNull() {
            assertThrows(InvalidUrlException.class, () ->
                    Url.restore(VALID_ID, null, VALID_SHORT_CODE, LocalDateTime.now(), null));
        }

        @Test
        @DisplayName("Should throw InvalidUrlException when originalUrl is blank")
        void shouldThrowWhenOriginalUrlIsBlank() {
            assertThrows(InvalidUrlException.class, () ->
                    Url.restore(VALID_ID, "  ", VALID_SHORT_CODE, LocalDateTime.now(), null));
        }

        @Test
        @DisplayName("Should throw InvalidUrlException when originalUrl has invalid protocol")
        void shouldThrowWhenOriginalUrlHasInvalidProtocol() {
            assertThrows(InvalidUrlException.class, () ->
                    Url.restore(VALID_ID, "ftp://example.com", VALID_SHORT_CODE, LocalDateTime.now(), null));
        }

        @Test
        @DisplayName("Should throw DomainException when shortCode is null")
        void shouldThrowWhenShortCodeIsNull() {
            assertThrows(DomainException.class, () ->
                    Url.restore(VALID_ID, VALID_ORIGINAL_URL, null, LocalDateTime.now(), null));
        }

        @Test
        @DisplayName("Should throw DomainException when shortCode is blank")
        void shouldThrowWhenShortCodeIsBlank() {
            assertThrows(DomainException.class, () ->
                    Url.restore(VALID_ID, VALID_ORIGINAL_URL, "  ", LocalDateTime.now(), null));
        }
    }

    @Nested
    @DisplayName("Url.create() tests")
    class CreateTests {

        @Test
        @DisplayName("Should create a Url with expirationDate from strategy")
        void shouldCreateUrlWithExpiration() {
            // Arrange
            ExpirationCalculationStrategy strategy = createdAt -> createdAt.plusDays(7);

            // Act
            Url url = Url.create(VALID_ID, VALID_ORIGINAL_URL, VALID_SHORT_CODE, strategy);

            // Assert
            assertNotNull(url.getCreatedAt());
            assertNotNull(url.getExpirationDate());
            assertEquals(url.getCreatedAt().plusDays(7), url.getExpirationDate());
        }

        @Test
        @DisplayName("Should create a lifetime Url when strategy returns null")
        void shouldCreateLifetimeUrlWhenStrategyReturnsNull() {
            // Arrange
            ExpirationCalculationStrategy neverExpireStrategy = createdAt -> null;

            // Act
            Url url = Url.create(VALID_ID, VALID_ORIGINAL_URL, VALID_SHORT_CODE, neverExpireStrategy);

            // Assert
            assertNotNull(url.getCreatedAt());
            assertNull(url.getExpirationDate());
        }
    }

    @Nested
    @DisplayName("isExpired() tests")
    class IsExpiredTests {

        @Test
        @DisplayName("Should return true when expirationDate is in the past")
        void shouldReturnTrueWhenExpired() {
            // Arrange - URL expired yesterday
            LocalDateTime createdAt = LocalDateTime.now().minusDays(10);
            LocalDateTime expirationDate = LocalDateTime.now().minusDays(1);
            Url url = Url.restore(VALID_ID, VALID_ORIGINAL_URL, VALID_SHORT_CODE, createdAt, expirationDate);

            // Act & Assert
            assertTrue(url.isExpired());
        }

        @Test
        @DisplayName("Should return false when expirationDate is in the future")
        void shouldReturnFalseWhenNotExpired() {
            // Arrange - URL expires in 7 days
            LocalDateTime createdAt = LocalDateTime.now();
            LocalDateTime expirationDate = LocalDateTime.now().plusDays(7);
            Url url = Url.restore(VALID_ID, VALID_ORIGINAL_URL, VALID_SHORT_CODE, createdAt, expirationDate);

            // Act & Assert
            assertFalse(url.isExpired());
        }

        @Test
        @DisplayName("Should return false when expirationDate is null (lifetime URL)")
        void shouldReturnFalseWhenLifetime() {
            // Arrange - Lifetime URL (no expiration)
            Url url = Url.restore(VALID_ID, VALID_ORIGINAL_URL, VALID_SHORT_CODE, LocalDateTime.now(), null);

            // Act & Assert
            assertFalse(url.isExpired());
        }
    }
}
