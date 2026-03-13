package pablo.tzeliks.blink_link.infrastructure.web.common;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pablo.tzeliks.blink_link.domain.common.exception.AuthenticationException;
import pablo.tzeliks.blink_link.domain.common.exception.InvalidResourceException;
import pablo.tzeliks.blink_link.domain.common.exception.ResourceNotFoundException;
import pablo.tzeliks.blink_link.domain.url.exception.InvalidUrlException;
import pablo.tzeliks.blink_link.domain.url.exception.UrlExpiredException;
import pablo.tzeliks.blink_link.domain.url.exception.UrlNotFoundException;
import pablo.tzeliks.blink_link.infrastructure.web.dto.ErrorResponse;
import pablo.tzeliks.blink_link.infrastructure.web.dto.ValidationError;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Global exception handler for the BlinkLink application.
 * <p>
 * This {@code @RestControllerAdvice} component intercepts exceptions thrown by
 * controllers and converts them into standardized HTTP error responses. It ensures
 * consistent error handling across the entire API and provides meaningful error
 * messages to clients in a structured format conforming to RFC 7807 (Problem Details for HTTP APIs).
 * <p>
 * <b>Handled Exception Types:</b>
 * <ul>
 *   <li>{@link UrlNotFoundException} - Returns 404 Not Found</li>
 *   <li>{@link InvalidUrlException} - Returns 400 Bad Request</li>
 *   <li>{@link MethodArgumentNotValidException} - Returns 422 Unprocessable Content with validation details</li>
 *   <li>{@link HttpMessageNotReadableException} - Returns 400 Bad Request for malformed JSON</li>
 *   <li>{@link Exception} - Returns 500 Internal Server Error for unexpected errors</li>
 * </ul>
 * <p>
 * All error responses follow a consistent structure defined by {@link ErrorResponse},
 * including timestamp, status code, error details, and request path. For validation
 * errors, the response includes a list of field-level validation failures.
 *
 * @author Pablo Tzeliks
 * @version 2.0.0
 * @since 1.0.0
 * @see ErrorResponse
 * @see ValidationError
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles exceptions when a requested URL is not found in the database.
     * <p>
     * This handler is invoked when a client attempts to resolve or access a short code
     * that doesn't exist in the system. It returns a structured error response with
     * HTTP status 404 (Not Found).
     *
     * @param ex the exception containing details about the missing URL
     * @param request the HTTP servlet request that triggered the exception
     * @return a {@link ResponseEntity} with status 404 and an {@link ErrorResponse} body
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {

        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                "Resource Not Found",
                ex.getMessage(),
                request.getRequestURI(),
                null
        );
    }

    /**
     * Handles exceptions related to invalid URL formats or business rule violations.
     * <p>
     * This handler catches validation errors at the domain level, such as URLs that
     * don't start with http/https, empty short codes, or other business rule violations.
     * It returns a structured error response with HTTP status 400 (Bad Request).
     *
     * @param ex the exception containing details about the invalid URL
     * @param request the HTTP servlet request that triggered the exception
     * @return a {@link ResponseEntity} with status 400 and an {@link ErrorResponse} body
     */
    @ExceptionHandler(InvalidResourceException.class)
    public ResponseEntity<ErrorResponse> handleInvalidResource(InvalidResourceException ex, HttpServletRequest request) {

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid Argument",
                ex.getMessage(),
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(UrlExpiredException.class)
    public ResponseEntity<ErrorResponse> handleExpiredUrl(UrlExpiredException ex, HttpServletRequest request) {

        return buildErrorResponse(
                HttpStatus.GONE,
                "Url Expired",
                ex.getMessage(),
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationError(AuthenticationException ex, HttpServletRequest request) {

        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                "Invalid Credentials",
                ex.getMessage(),
                request.getRequestURI(),
                null
        );
    }

    /**
     * Handles Bean Validation failures on request objects.
     * <p>
     * This handler is triggered when {@code @Valid} annotation fails on request DTOs
     * due to constraint violations (e.g., {@code @NotBlank}, {@code @URL}). It extracts
     * all field-level validation errors and returns them in a structured format with
     * HTTP status 422 (Unprocessable Content).
     * <p>
     * The response includes a list of {@link ValidationError} objects, each containing
     * the field name and the validation error message.
     *
     * @param ex the exception containing details about validation failures
     * @param request the HTTP servlet request that triggered the exception
     * @return a {@link ResponseEntity} with status 422 and an {@link ErrorResponse} body
     *         containing detailed validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<ValidationError> errors = ex.getBindingResult().getFieldErrors().
                stream()
                .map(fieldError -> new ValidationError(
                        fieldError.getField(),
                        fieldError.getDefaultMessage()
                ))
                .toList();

        return buildErrorResponse(
                HttpStatus.UNPROCESSABLE_CONTENT,
                "Validation Failed",
                "One or more validation errors occurred.",
                request.getRequestURI(),
                errors
        );
    }

    /**
     * Handles JSON parsing errors in request bodies.
     * <p>
     * This handler is triggered when the client sends malformed JSON that cannot
     * be deserialized into the expected request object. Common causes include
     * syntax errors, type mismatches, or missing required fields.
     * It returns HTTP status 400 (Bad Request).
     *
     * @param ex the exception containing details about the JSON parsing failure
     * @param request the HTTP servlet request that triggered the exception
     * @return a {@link ResponseEntity} with status 400 and an {@link ErrorResponse} body
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleJsonError(HttpMessageNotReadableException ex, HttpServletRequest request) {

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Malformed JSON Request",
                "The request body is invalid or malformed.",
                request.getRequestURI(),
                null
        );
    }

    /**
     * Handles unexpected exceptions that are not explicitly handled elsewhere.
     * <p>
     * This is the catch-all handler for any uncaught exceptions in the application.
     * It logs the full exception details for debugging purposes and returns a generic
     * error message to the client with HTTP status 500 (Internal Server Error).
     * <p>
     * <b>Security Note:</b> The actual exception details are logged server-side but
     * not exposed to the client to prevent information leakage.
     *
     * @param ex the unexpected exception
     * @param request the HTTP servlet request that triggered the exception
     * @return a {@link ResponseEntity} with status 500 and a generic {@link ErrorResponse}
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {

        LOGGER.error("Unexpected error occurred at {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "An unexpected error occurred. Please try again later.",
                request.getRequestURI(),
                null
        );
    }

    // Utility method for building error responses

    /**
     * Builds a standardized error response.
     * <p>
     * This utility method constructs an {@link ErrorResponse} object with all required
     * fields and wraps it in a {@link ResponseEntity} with the appropriate HTTP status.
     * The error response follows RFC 7807 (Problem Details for HTTP APIs) conventions.
     *
     * @param status the HTTP status code for the response
     * @param title a short, human-readable summary of the error type
     * @param detail a human-readable explanation specific to this occurrence of the error
     * @param instance the URI reference that identifies the specific occurrence of the error
     * @param errors optional list of field-level validation errors; can be {@code null}
     * @return a {@link ResponseEntity} containing the error response and status code
     */
    private ResponseEntity<ErrorResponse> buildErrorResponse(

            HttpStatus status,
            String title,
            String detail,
            String instance,
            List<ValidationError> errors
    ) {

        ErrorResponse response = new ErrorResponse(

                "about:blank",
                title,
                status.value(),
                detail,
                instance,
                LocalDateTime.now(),
                errors
        );

        return ResponseEntity.status(status).body(response);
    }
}