package pablo.tzeliks.blink_link.infrastructure.web.dto;

/**
 * Data Transfer Object for field-level validation errors.
 * <p>
 * This record represents a single validation error on a specific field,
 * typically used within an {@link ErrorResponse} when request validation fails.
 * It provides granular information about which field failed validation and why.
 * <p>
 * <b>Example JSON (as part of an error response):</b>
 * <pre>
 * "errors": [
 *   {
 *     "field": "originalUrl",
 *     "message": "Original URL must not be blank"
 *   },
 *   {
 *     "field": "originalUrl",
 *     "message": "Original URL must be a valid URL"
 *   }
 * ]
 * </pre>
 *
 * @param field the name of the field that failed validation
 * @param message the validation error message explaining why the field is invalid
 * @author Pablo Tzeliks
 * @version 2.0.0
 * @since 1.0.0
 * @see ErrorResponse
 */
public record ValidationError(

        String field,
        String message
) { }
