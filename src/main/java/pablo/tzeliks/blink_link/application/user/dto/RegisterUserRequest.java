package pablo.tzeliks.blink_link.application.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterUserRequest(

        @JsonProperty("email")
        @Email(message = "Email must be valid.")
        @NotBlank(message = "Email must not be blank.")
        String email,

        @JsonProperty("password")
        @NotBlank(message = "Password must not be blank.")
        String password
) { }
