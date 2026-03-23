package pablo.tzeliks.blink_link.infrastructure.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Pablo Tzeliks
 * @version 2.0.0
 * @since 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standardized error response based on RFC 7807 Problem Details")
public record ErrorResponse(

        @Schema(description = "A URI reference that identifies the problem type", example = "about:blank")
        String type,

        @Schema(description = "A short, human-readable summary of the problem type", example = "Validation Failed")
        String title,

        @Schema(description = "The HTTP status code", example = "422")
        int status,

        @Schema(description = "A human-readable explanation specific to this occurrence of the problem", example = "One or more validation errors occurred.")
        String detail,

        @Schema(description = "A URI reference that identifies the specific occurrence of the problem", example = "/api/v3/auth/register")
        String instance,

        @Schema(description = "The time the error occurred")
        LocalDateTime timestamp,

        @Schema(description = "A list of specific validation errors, if applicable")
        List<ValidationError> errors
) { }
