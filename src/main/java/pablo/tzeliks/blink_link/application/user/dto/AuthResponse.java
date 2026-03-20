package pablo.tzeliks.blink_link.application.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record AuthResponse(

        @JsonProperty("id")
        UUID id,

        @JsonProperty("email")
        String email,

        @JsonProperty("role")
        String role,

        @JsonProperty("plan")
        String plan,

        @JsonProperty("createdAt")
        String createdAt,

        @JsonProperty("updatedAt")
        String updatedAt
) { }
