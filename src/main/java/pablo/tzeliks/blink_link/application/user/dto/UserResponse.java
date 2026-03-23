package pablo.tzeliks.blink_link.application.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Data transfer object representing a user's profile information")
public record UserResponse(

        @Schema(description = "Unique identifier of the user (UUID)", example = "123e4567-e89b-12d3-a456-426614174000")
        @JsonProperty("id")
        String id,

        @Schema(description = "User's registered email address", example = "user@example.com")
        @JsonProperty("email")
        String email,

        @Schema(description = "User's authorization role", example = "USER")
        @JsonProperty("role")
        String role,

        @Schema(description = "User's current subscription plan", example = "FREE")
        @JsonProperty("plan")
        String plan,

        @Schema(description = "Timestamp when the user account was created", example = "2024-03-23T10:15:30Z", type = "string")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
        @JsonProperty("created_at")
        LocalDateTime createdAt,

        @Schema(description = "Timestamp when the user account was last updated", example = "2024-03-23T10:15:30Z", type = "string")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
        @JsonProperty("updated_at")
        LocalDateTime updatedAt
) { }