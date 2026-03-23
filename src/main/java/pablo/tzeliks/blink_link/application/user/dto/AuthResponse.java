package pablo.tzeliks.blink_link.application.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Data transfer object containing the user profile and the authentication token")
public record AuthResponse(

        @Schema(description = "The profile details of the authenticated user")
        @JsonProperty("user_profile")
        UserResponse userProfile,

        @Schema(description = "JWT authentication token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjNlN...")
        @JsonProperty("token")
        String token
) { }