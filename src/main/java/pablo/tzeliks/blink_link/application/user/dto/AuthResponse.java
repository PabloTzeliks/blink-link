package pablo.tzeliks.blink_link.application.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthResponse(

        @JsonProperty("user_profile")
        UserResponse userProfile,

        @JsonProperty("token")
        String token
) { }
