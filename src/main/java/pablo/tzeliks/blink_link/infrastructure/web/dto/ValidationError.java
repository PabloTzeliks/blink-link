package pablo.tzeliks.blink_link.infrastructure.web.dto;

public record ValidationError(

        String field,
        String message
) { }
