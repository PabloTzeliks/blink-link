package pablo.tzeliks.blink_link.application.url.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

/**
 * Data Transfer Object for URL creation requests.
 * <p>
 * This record represents the request payload for creating a new shortened URL.
 * It contains the original long URL that the client wants to shorten.
 * <p>
 * <b>Validation Rules:</b>
 * <ul>
 *   <li>{@code @NotBlank}: The URL cannot be null, empty, or contain only whitespace</li>
 *   <li>{@code @URL}: The URL must conform to valid URL format (enforced by Hibernate Validator)</li>
 *   <li>Domain-level validation also requires URLs to start with http:// or https://</li>
 * </ul>
 * <p>
 * <b>Example JSON Request:</b>
 * <pre>
 * {
 *   "original_url": "https://example.com/very/long/url/path"
 * }
 * </pre>
 *
 * @param originalUrl the original long URL to be shortened; must be a valid, non-blank URL string
 * @author Pablo Tzeliks
 * @version 2.0.0
 * @since 1.0.0
 */
public record CreateUrlRequest(

        @JsonProperty("original_url")
        @NotBlank(message = "Original URL must not be blank")
        @URL(message = "Original URL must be a valid URL")
        String originalUrl
) { }
