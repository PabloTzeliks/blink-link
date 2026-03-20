package pablo.tzeliks.blink_link.application.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record ChangePlanRequest(

        @JsonProperty("plan")
        @NotBlank(message = "Plan must not be blank.")
        String plan
) { }
