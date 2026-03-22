package pablo.tzeliks.blink_link.infrastructure.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Pablo Tzeliks
 * @version 2.0.0
 * @since 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(

        String type,
        String title,
        int status,
        String detail,
        String instance,
        LocalDateTime timestamp,
        List<ValidationError> errors
) { }
