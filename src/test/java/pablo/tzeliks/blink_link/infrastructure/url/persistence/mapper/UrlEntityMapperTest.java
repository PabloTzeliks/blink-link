package pablo.tzeliks.blink_link.infrastructure.url.persistence.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pablo.tzeliks.blink_link.domain.url.model.Url;
import pablo.tzeliks.blink_link.infrastructure.url.persistence.entity.UrlEntity;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link UrlEntityMapper}.
 * <p>
 * Validates that expirationDate is correctly mapped between
 * Domain ↔ Entity in both directions.
 *
 * @since 3.0.0
 */
class UrlEntityMapperTest {

    private final UrlEntityMapper mapper = new UrlEntityMapper();

    @Test
    @DisplayName("toEntity: Should map all fields including expirationDate from Domain to Entity")
    void shouldMapDomainToEntity() {
        // Arrange
        LocalDateTime createdAt = LocalDateTime.of(2026, 3, 1, 10, 0, 0);
        LocalDateTime expirationDate = createdAt.plusDays(7);
        Url domain = Url.restore(1L, "https://github.com", "abc123", createdAt, expirationDate);

        // Act
        UrlEntity entity = mapper.toEntity(domain);

        // Assert
        assertEquals(1L, entity.getId());
        assertEquals("https://github.com", entity.getOriginalUrl());
        assertEquals("abc123", entity.getShortCode());
        assertEquals(createdAt, entity.getCreatedAt());
        assertEquals(expirationDate, entity.getExpirationDate());
    }

    @Test
    @DisplayName("toDomain: Should map all fields including expirationDate from Entity to Domain")
    void shouldMapEntityToDomain() {
        // Arrange
        LocalDateTime createdAt = LocalDateTime.of(2026, 3, 1, 10, 0, 0);
        LocalDateTime expirationDate = createdAt.plusDays(7);
        UrlEntity entity = new UrlEntity(1L, "https://github.com", "abc123", createdAt, expirationDate);

        // Act
        Url domain = mapper.toDomain(entity);

        // Assert
        assertEquals(1L, domain.getId());
        assertEquals("https://github.com", domain.getOriginalUrl());
        assertEquals("abc123", domain.getShortCode());
        assertEquals(createdAt, domain.getCreatedAt());
        assertEquals(expirationDate, domain.getExpirationDate());
    }

    @Test
    @DisplayName("Round-trip: Domain → Entity → Domain should preserve all fields")
    void shouldPreserveFieldsOnRoundTrip() {
        // Arrange
        LocalDateTime createdAt = LocalDateTime.of(2026, 6, 15, 14, 30);
        LocalDateTime expirationDate = createdAt.plusYears(1);
        Url original = Url.restore(42L, "https://example.com", "short1", createdAt, expirationDate);

        // Act
        UrlEntity entity = mapper.toEntity(original);
        Url restored = mapper.toDomain(entity);

        // Assert
        assertEquals(original.getId(), restored.getId());
        assertEquals(original.getOriginalUrl(), restored.getOriginalUrl());
        assertEquals(original.getShortCode(), restored.getShortCode());
        assertEquals(original.getCreatedAt(), restored.getCreatedAt());
        assertEquals(original.getExpirationDate(), restored.getExpirationDate());
    }
}
