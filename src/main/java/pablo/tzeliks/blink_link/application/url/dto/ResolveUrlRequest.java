package pablo.tzeliks.blink_link.application.url.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object for URL resolution requests.
 * <p>
 * This record represents the request payload for resolving a short code
 * back to its original URL. It contains the short code to look up.
 * <p>
 * <b>Validation Rules:</b>
 * <ul>
 *   <li>{@code @NotBlank}: The short code cannot be null, empty, or contain only whitespace</li>
 * </ul>
 * <p>
 * <b>Example JSON Request:</b>
 * <pre>
 * {
 *   "short_code": "abc123"
 * }
 * </pre>
 *
 * @param shortCode the short code to resolve; must be a non-blank string
 * @author Pablo Tzeliks
 * @version 2.0.0
 * @since 1.0.0
 */
public record ResolveUrlRequest(

        @JsonProperty("short_code")
        @NotBlank(message = "Short Code must not be blank")
        String shortCode
) { }
