package pablo.tzeliks.blink_link.application.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Payload for login an user")
public record LoginUserRequest(

        @Schema(description = "The user's email address", example = "john.doe@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @JsonProperty("email")
        @Email(message = "Email must be valid.")
        @NotBlank(message = "Email must not be blank.")
        String email,

        @Schema(description = "The user password", example = "Str0ngP@ssw0rd!", requiredMode = Schema.RequiredMode.REQUIRED)
        @JsonProperty("password")
        @NotBlank(message = "Password must not be blank.")
        String rawPassword
) { }
