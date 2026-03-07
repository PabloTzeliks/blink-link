package pablo.tzeliks.blink_link.application.url.mapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pablo.tzeliks.blink_link.application.url.dto.UrlResponse;
import pablo.tzeliks.blink_link.domain.url.model.Url;

/**
 * Mapper for converting between URL DTOs and domain models.
 * <p>
 * This component handles the transformation of data between the application layer
 * (DTOs) and the domain layer (domain models). It enriches DTOs with additional
 * information such as the complete short URL including the base domain.
 * <p>
 * The base URL is configured via the {@code blink-link.base-url} property and is
 * injected at runtime, allowing for environment-specific configuration (e.g.,
 * {@code https://localhost:8080} for development, {@code https://blink.link} for production).
 *
 * @author Pablo Tzeliks
 * @version 2.0.0
 * @since 1.0.0
 */
@Component
public class UrlDtoMapper {

    /**
     * Base URL for constructing complete shortened URLs.
     * <p>
     * This value is injected from the application configuration property
     * {@code blink-link.base-url} and is used to create the full short URL
     * by appending the short code (e.g., "https://blink.link" + "/abc123").
     */
    @Value("${blink-link.base-url}")
    private String baseUrl;

    /**
     * Converts a domain {@link Url} object to a {@link UrlResponse} DTO.
     * <p>
     * This method transforms the domain model into a response DTO by:
     * <ol>
     *   <li>Extracting the domain model fields</li>
     *   <li>Constructing the complete short URL by combining the base URL with the short code</li>
     *   <li>Handling trailing slashes in the base URL to ensure proper URL formation</li>
     * </ol>
     *
     * @param domain the domain model containing URL information
     * @return a {@link UrlResponse} DTO with all fields populated, including the complete short URL
     */
    public UrlResponse toDto(Url domain) {

        String shortUrl = baseUrl.endsWith("/")
                ? baseUrl + domain.getShortCode()
                : baseUrl + "/" + domain.getShortCode();

        return new UrlResponse(
                domain.getOriginalUrl(),
                domain.getShortCode(),
                shortUrl,
                domain.getCreatedAt()
        );
    }
}
