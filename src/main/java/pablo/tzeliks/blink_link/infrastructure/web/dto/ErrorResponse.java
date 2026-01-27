package pablo.tzeliks.blink_link.infrastructure.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for error responses.
 * <p>
 * This record represents a standardized error response structure following
 * RFC 7807 (Problem Details for HTTP APIs). It provides consistent error
 * information to API clients when requests fail.
 * <p>
 * The {@code @JsonInclude(JsonInclude.Include.NON_NULL)} annotation ensures
 * that {@code null} fields are excluded from the JSON response, keeping
 * responses concise.
 * <p>
 * <b>Example JSON Response:</b>
 * <pre>
 * {
 *   "type": "about:blank",
 *   "title": "Resource Not Found",
 *   "status": 404,
 *   "detail": "URL not found for the provided short code: xyz123",
 *   "instance": "/api/v2/urls/xyz123",
 *   "timestamp": "2026-01-27T20:30:00"
 * }
 * </pre>
 *
 * @param type a URI reference that identifies the problem type (typically "about:blank" for generic errors)
 * @param title a short, human-readable summary of the problem type
 * @param status the HTTP status code for this occurrence of the problem
 * @param detail a human-readable explanation specific to this occurrence of the problem
 * @param instance a URI reference that identifies the specific occurrence of the problem (usually the request path)
 * @param timestamp the date and time when the error occurred
 * @param errors optional list of field-level validation errors; present only for validation failures
 * @author Pablo Tzeliks
 * @version 2.0.0
 * @since 1.0.0
 * @see ValidationError
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
