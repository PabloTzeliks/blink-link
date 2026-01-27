package pablo.tzeliks.blink_link.application.url.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public record ResolveUrlRequest(

        @JsonProperty("short_code")
        @NotBlank(message = "Short Code must not be blank")
        String shortCode
) { }
