package pablo.tzeliks.blink_link.application.url.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public record ResolveUrlRequest(

        @JsonProperty("short_url")
        @NotBlank(message = "Short URL must not be blank")
        @URL(message = "Short URL must be a valid URL")
        String shortUrl
) { }
