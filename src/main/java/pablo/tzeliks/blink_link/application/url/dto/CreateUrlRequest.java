package pablo.tzeliks.blink_link.application.url.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

/**
 * @author Pablo Tzeliks
 * @version 3.0.0
 * @since 1.0.0
 */

@Schema(description = "Payload to create a new shortened URL")
public record CreateUrlRequest(

        @Schema(description = "The destination URL to be shortened", example = "https://github.com/PabloTzeliks/blink-link", requiredMode = Schema.RequiredMode.REQUIRED)
        @JsonProperty("original_url")
        @NotBlank(message = "Original URL must not be blank")
        @URL(message = "Original URL must be a valid URL")
        String originalUrl
) { }
