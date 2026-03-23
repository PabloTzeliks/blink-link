package pablo.tzeliks.blink_link.application.url.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * @author Pablo Tzeliks
 * @version 3.0.0
 * @since 1.0.0
 */
@Schema(description = "Data transfer object representing the details of a shortened URL")
public record UrlResponse(

        @Schema(description = "The original, long URL that was shortened", example = "https://www.example.com/some/very/long/article/path")
        @JsonProperty("original_url")
        String originalUrl,

        @Schema(description = "The unique short code generated for the URL", example = "AbC123Xy")
        @JsonProperty("short_code")
        String shortCode,

        @Schema(description = "The complete shortened URL ready to be shared", example = "https://blink-link.com/AbC123Xy")
        @JsonProperty("short_url")
        String shortUrl,

        @Schema(description = "Timestamp when the short URL was created", example = "2026-03-23T10:15:30Z", type = "string")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
        @JsonProperty("created_at")
        LocalDateTime createdAt,

        @Schema(description = "Timestamp when the short URL will expire (if applicable)", example = "2026-04-23T10:15:30Z", type = "string")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
        @JsonProperty("expiration_date")
        LocalDateTime expirationDate
) { }