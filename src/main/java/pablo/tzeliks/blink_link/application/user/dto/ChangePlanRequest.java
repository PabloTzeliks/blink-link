package pablo.tzeliks.blink_link.application.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import pablo.tzeliks.blink_link.domain.user.model.Plan;

public record ChangePlanRequest(

        @JsonProperty("plan")
        @NotNull
        Plan plan
) { }
