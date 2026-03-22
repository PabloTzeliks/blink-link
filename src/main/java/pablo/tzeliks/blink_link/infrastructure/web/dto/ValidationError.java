package pablo.tzeliks.blink_link.infrastructure.web.dto;

/**
 * @author Pablo Tzeliks
 * @version 2.0.0
 * @since 1.0.0
 */
public record ValidationError(

        String field,
        String message
) { }
