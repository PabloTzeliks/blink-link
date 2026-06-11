package pablo.tzeliks.blink_link.application.url.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;
import org.jspecify.annotations.Nullable;

/**
 * @author Pablo Tzeliks
 * @version 2.0.0
 * @since 1.0.0
 */
public record CreateShortCodeRequest(

        @JsonProperty("original_url")
        @NotBlank(message = "Original URL must not be blank")
        @URL(message = "Original URL must be a valid URL")
        String originalUrl,

        @JsonProperty("custom_code")
        @Nullable
        String customCode
) { }
