package pablo.tzeliks.blink_link.infrastructure.url.persistence.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pablo.tzeliks.blink_link.domain.url.model.Url;
import pablo.tzeliks.blink_link.infrastructure.url.persistence.entity.UrlEntity;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the {@link UrlEntityMapper}.
 * <p>
 * Validates bi-directional mapping between Url domain objects and UrlEntity persistence objects,
 * including the expirationDate field.
 *
 * @author QA Test Suite
 * @since 3.0.0
 */
class UrlEntityMapperTest {

    private final UrlEntityMapper mapper = new UrlEntityMapper();

    @Test
    @DisplayName("Should map Url domain to UrlEntity including expirationDate")
    void shouldMapDomainToEntity() {
        // Arrange
        LocalDateTime createdAt = LocalDateTime.of(2026, 3, 1, 10, 0, 0);
        LocalDateTime expirationDate = LocalDateTime.of(2026, 3, 8, 10, 0, 0);
        Url domain = Url.restore(1L, "https://example.com", "abc123", createdAt, expirationDate);

        // Act
        UrlEntity entity = mapper.toEntity(domain);

        // Assert
        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getOriginalUrl()).isEqualTo("https://example.com");
        assertThat(entity.getShortCode()).isEqualTo("abc123");
        assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
        assertThat(entity.getExpirationDate()).isEqualTo(expirationDate);
    }

    @Test
    @DisplayName("Should map UrlEntity to Url domain including expirationDate")
    void shouldMapEntityToDomain() {
        // Arrange
        LocalDateTime createdAt = LocalDateTime.of(2026, 3, 1, 10, 0, 0);
        LocalDateTime expirationDate = LocalDateTime.of(2026, 3, 8, 10, 0, 0);
        UrlEntity entity = new UrlEntity(2L, "https://github.com", "def456", createdAt, expirationDate);

        // Act
        Url domain = mapper.toDomain(entity);

        // Assert
        assertThat(domain.getId()).isEqualTo(2L);
        assertThat(domain.getOriginalUrl()).isEqualTo("https://github.com");
        assertThat(domain.getShortCode()).isEqualTo("def456");
        assertThat(domain.getCreatedAt()).isEqualTo(createdAt);
        assertThat(domain.getExpirationDate()).isEqualTo(expirationDate);
    }

    @Test
    @DisplayName("Should round-trip Url domain -> UrlEntity -> Url domain preserving all fields")
    void shouldRoundTripPreservingAllFields() {
        // Arrange
        LocalDateTime createdAt = LocalDateTime.of(2026, 6, 15, 14, 30, 0);
        LocalDateTime expirationDate = LocalDateTime.of(2026, 6, 22, 14, 30, 0);
        Url original = Url.restore(3L, "https://linkedin.com/profile", "ghi789", createdAt, expirationDate);

        // Act
        UrlEntity entity = mapper.toEntity(original);
        Url restored = mapper.toDomain(entity);

        // Assert
        assertThat(restored.getId()).isEqualTo(original.getId());
        assertThat(restored.getOriginalUrl()).isEqualTo(original.getOriginalUrl());
        assertThat(restored.getShortCode()).isEqualTo(original.getShortCode());
        assertThat(restored.getCreatedAt()).isEqualTo(original.getCreatedAt());
        assertThat(restored.getExpirationDate()).isEqualTo(original.getExpirationDate());
    }
}
