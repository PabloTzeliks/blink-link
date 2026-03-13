package pablo.tzeliks.blink_link.application.url.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for URL responses.
 * <p>
 * This record represents the response payload for URL operations (both creation
 * and resolution). It contains comprehensive information about a shortened URL,
 * including the original URL, short code, complete short URL, and creation timestamp.
 * <p>
 * <b>Example JSON Response:</b>
 * <pre>
 * {
 *   "original_url": "https://example.com/very/long/url/path",
 *   "short_code": "abc123",
 *   "short_url": "https://blink.link/abc123",
 *   "created_at": "2026-01-27T20:30:00"
 * }
 * </pre>
 *
 * @param originalUrl the original long URL that was shortened
 * @param shortCode the Base62-encoded short code (e.g., "abc123")
 * @param shortUrl the complete shortened URL including the base domain (e.g., "https://blink.link/abc123")
 * @param createdAt the timestamp when this URL was created in the system
 * @author Pablo Tzeliks
 * @version 2.0.0
 * @since 1.0.0
 */
public record UrlResponse(
        @JsonProperty("original_url") String originalUrl,
        @JsonProperty("short_code") String shortCode,
        @JsonProperty("short_url") String shortUrl,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
        @JsonProperty("created_at") LocalDateTime createdAt,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
        @JsonProperty("expiration_date") LocalDateTime expirationDate
) { }
