package pablo.tzeliks.blink_link.infrastructure.web.common;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import pablo.tzeliks.blink_link.application.url.exception.SequenceGenerationException;
import pablo.tzeliks.blink_link.application.url.exception.UrlCollisionException;
import pablo.tzeliks.blink_link.domain.common.exception.*;
import pablo.tzeliks.blink_link.domain.url.exception.InvalidUrlException;
import pablo.tzeliks.blink_link.domain.url.exception.UrlExpiredException;
import pablo.tzeliks.blink_link.domain.url.exception.UrlNotFoundException;
import pablo.tzeliks.blink_link.infrastructure.web.dto.ErrorResponse;
import pablo.tzeliks.blink_link.infrastructure.web.dto.ValidationError;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Pablo Tzeliks
 * @version 2.0.0
 * @since 1.0.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRuleError(BusinessRuleException ex, HttpServletRequest request) {

        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "Business Rule Error",
                ex.getMessage(),
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler({AuthorizationException.class, AuthorizationDeniedException.class})
    public ResponseEntity<ErrorResponse> handleAuthorizationError(Exception ex, HttpServletRequest request) {

        return buildErrorResponse(
                HttpStatus.FORBIDDEN,
                "Access Denied",
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

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException ex, HttpServletRequest request) {

        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                "Resource Not Found",
                "The requested endpoint or resource does not exist.",
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(SequenceGenerationException.class)
    public ResponseEntity<ErrorResponse> handleSequenceGenerationError(SequenceGenerationException ex, HttpServletRequest request) {

        return buildErrorResponse(
                HttpStatus.SERVICE_UNAVAILABLE,
                "ID Generation Failed",
                "Service unavailable at this moment. Not able to generate a Short Url.",
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {

        return buildErrorResponse(
                HttpStatus.METHOD_NOT_ALLOWED,
                "Method Not Allowed",
                String.format("The HTTP method '%s' is not supported for this endpoint.", ex.getMethod()),
                request.getRequestURI(),
                null
        );
    }

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