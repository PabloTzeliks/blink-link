package pablo.tzeliks.blink_link.application.url.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

/**
 * @author Pablo Tzeliks
 * @version 2.0.0
 * @since 1.0.0
 */
public record ResolveShortCodeRequest(

        @JsonProperty("short_code")
        @NotBlank(message = "Short Code must not be blank")
        String shortCode
) { }
