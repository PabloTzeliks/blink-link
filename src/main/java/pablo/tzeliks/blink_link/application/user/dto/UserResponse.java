package pablo.tzeliks.blink_link.application.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record UserResponse(
        @JsonProperty("id") String id,
        @JsonProperty("email") String email,
        @JsonProperty("role") String role,
        @JsonProperty("plan") String plan,
        @JsonProperty("created_at") LocalDateTime createdAt,
        @JsonProperty("updated_at") LocalDateTime updatedAt
) { }
