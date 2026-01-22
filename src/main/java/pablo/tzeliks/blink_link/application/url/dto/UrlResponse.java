package pablo.tzeliks.blink_link.application.url.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * TODO : new Docs
 *
 * @author Pablo Tzeliks
 * @version 2.0.0
 * @since 1.0.0
 */
public record UrlResponse(
        @JsonProperty("original_url") String originalUrl,
        @JsonProperty("short_url") String shortUrl,
        @JsonProperty("created_at") LocalDateTime createdAt
) { }
