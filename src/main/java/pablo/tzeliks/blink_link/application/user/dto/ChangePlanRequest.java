package pablo.tzeliks.blink_link.application.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Payload for changing a user's subscription plan")
public record ChangePlanRequest(

        @NotBlank(message = "Plan cannot be blank")
        @Schema(description = "The new plan to assign to the user (e.g., FREE, PRO, ENTERPRISE)", example = "PRO", requiredMode = Schema.RequiredMode.REQUIRED)
        String plan
) {}
