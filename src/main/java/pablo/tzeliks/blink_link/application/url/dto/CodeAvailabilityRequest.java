package pablo.tzeliks.blink_link.application.url.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CodeAvailabilityRequest(

        @JsonProperty("custom_code")
        @NotBlank(message = "Original URL must not be blank")
        @Size(min = 4, max = 20, message = "Code must be between 4 and 20 characters")
        @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Code must contain only letters, digits, hyphens, or underscores")
        String customCode
) { }
