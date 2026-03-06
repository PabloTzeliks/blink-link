package pablo.tzeliks.blink_link.application.user.dto;

public record LoginUserRequest(
        String email,
        String rawPassword
) { }
