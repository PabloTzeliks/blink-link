package pablo.tzeliks.blink_link.application.url.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * @author Pablo Tzeliks
 * @version 3.0.0
 * @since 1.0.0
 */
@Schema(description = "Data transfer object used to request the resolution of a short code")
public record ResolveUrlRequest(

        @Schema(description = "The short code to be resolved into the original URL", example = "AbC123Xy", requiredMode = Schema.RequiredMode.REQUIRED)
        @JsonProperty("short_code")
        @NotBlank(message = "Short Code must not be blank")
        String shortCode
) { }