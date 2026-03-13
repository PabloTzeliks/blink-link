package pablo.tzeliks.blink_link.application.url.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import pablo.tzeliks.blink_link.application.url.dto.UrlResponse;
import pablo.tzeliks.blink_link.domain.url.model.Url;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link UrlDtoMapper}.
 * <p>
 * Validates that expirationDate is correctly mapped from Domain to DTO
 * and that the short URL is properly constructed.
 *
 * @since 3.0.0
 */
class UrlDtoMapperTest {

    private UrlDtoMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new UrlDtoMapper();
        ReflectionTestUtils.setField(mapper, "baseUrl", "http://localhost:8080/");
    }

    @Test
    @DisplayName("toDto: Should map all fields including expirationDate from Domain to DTO")
    void shouldMapDomainToDto() {
        // Arrange
        LocalDateTime createdAt = LocalDateTime.of(2026, 3, 1, 10, 0, 0);
        LocalDateTime expirationDate = createdAt.plusDays(7);
        Url domain = Url.restore(1L, "https://github.com", "abc123", createdAt, expirationDate);

        // Act
        UrlResponse dto = mapper.toDto(domain);

        // Assert
        assertEquals("https://github.com", dto.originalUrl());
        assertEquals("abc123", dto.shortCode());
        assertEquals("http://localhost:8080/abc123", dto.shortUrl());
        assertEquals(createdAt, dto.createdAt());
        assertEquals(expirationDate, dto.expirationDate());
    }

    @Test
    @DisplayName("toDto: Should handle null expirationDate (lifetime URL)")
    void shouldMapNullExpirationDate() {
        // Arrange
        LocalDateTime createdAt = LocalDateTime.of(2026, 3, 1, 10, 0, 0);
        Url domain = Url.restore(1L, "https://github.com", "abc123", createdAt, null);

        // Act
        UrlResponse dto = mapper.toDto(domain);

        // Assert
        assertNull(dto.expirationDate());
    }
}
