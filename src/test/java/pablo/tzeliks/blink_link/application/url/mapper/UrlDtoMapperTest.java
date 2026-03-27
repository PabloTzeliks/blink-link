package pablo.tzeliks.blink_link.application.url.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import pablo.tzeliks.blink_link.application.url.dto.UrlResponse;
import pablo.tzeliks.blink_link.domain.url.model.Url;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the {@link UrlDtoMapper}.
 * <p>
 * Validates that the mapper correctly converts a Url domain object to a UrlResponse DTO,
 * including the expirationDate field and the constructed short URL.
 *
 * @author QA Test Suite
 * @since 3.0.0
 */
class UrlDtoMapperTest {

    private UrlDtoMapper mapper;

    private static final String BASE_URL = "http://localhost:8080/";

    @BeforeEach
    void setUp() {
        mapper = new UrlDtoMapper();
        ReflectionTestUtils.setField(mapper, "baseUrl", BASE_URL);
    }

    @Test
    @DisplayName("Should map Url domain to UrlResponse DTO with all fields including expirationDate")
    void shouldMapDomainToDtoWithExpirationDate() {
        // Arrange
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 12, 0, 0);
        LocalDateTime expirationDate = LocalDateTime.of(2026, 1, 8, 12, 0, 0);
        UUID userId = UUID.randomUUID();

        Url domain = Url.restore(1L, userId, "https://example.com", "abc123", createdAt, expirationDate);

        // Act
        UrlResponse response = mapper.toDto(domain);

        // Assert
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.originalUrl()).isEqualTo("https://example.com");
        assertThat(response.shortCode()).isEqualTo("abc123");
        assertThat(response.shortUrl()).isEqualTo(BASE_URL + "abc123");
        assertThat(response.createdAt()).isEqualTo(createdAt);
        assertThat(response.expirationDate()).isEqualTo(expirationDate);
    }

    @Test
    @DisplayName("Should map Url domain with null expirationDate to DTO")
    void shouldMapDomainWithNullExpirationDateToDto() {
        // Arrange
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 12, 0, 0);
        UUID userId = UUID.randomUUID();

        Url domain = Url.restore(2L, userId, "https://example.com", "def456", createdAt, null);

        // Act
        UrlResponse response = mapper.toDto(domain);

        // Assert
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.originalUrl()).isEqualTo("https://example.com");
        assertThat(response.shortCode()).isEqualTo("def456");
        assertThat(response.shortUrl()).isEqualTo(BASE_URL + "def456");
        assertThat(response.createdAt()).isEqualTo(createdAt);
        assertThat(response.expirationDate()).isNull();
    }

    @Test
    @DisplayName("Should construct short URL correctly when base URL does not end with slash")
    void shouldConstructShortUrlWithoutTrailingSlash() {
        // Arrange
        ReflectionTestUtils.setField(mapper, "baseUrl", "http://localhost:8080");

        LocalDateTime createdAt = LocalDateTime.now();
        Url domain = Url.restore(3L, UUID.randomUUID(), "https://example.com", "ghi789", createdAt, createdAt.plusDays(7));

        // Act
        UrlResponse response = mapper.toDto(domain);

        // Assert
        assertThat(response.shortUrl()).isEqualTo("http://localhost:8080/ghi789");
    }
}
