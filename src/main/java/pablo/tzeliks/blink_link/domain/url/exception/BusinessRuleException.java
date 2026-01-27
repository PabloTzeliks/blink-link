package pablo.tzeliks.blink_link.domain.url.exception;

/**
 * Base exception class for business rule violations in the domain layer.
 * <p>
 * This exception serves as the parent class for all domain-specific exceptions
 * in the URL shortener application. It represents violations of business rules
 * or domain constraints that occur during the execution of business logic.
 * <p>
 * By extending {@link RuntimeException}, this exception is unchecked, allowing
 * it to propagate up the call stack without requiring explicit handling at every level.
 * This aligns with modern exception handling practices where business rule violations
 * are treated as exceptional conditions that should be handled at appropriate boundaries
 * (e.g., in controllers via {@code @ExceptionHandler}).
 * <p>
 * <b>Subclasses:</b>
 * <ul>
 *   <li>{@link InvalidUrlException} - Thrown when URL validation fails</li>
 *   <li>{@link UrlNotFoundException} - Thrown when a URL lookup fails</li>
 * </ul>
 *
 * @author Pablo Tzeliks
 * @version 2.0.0
 * @since 1.0.0
 * @see InvalidUrlException
 * @see UrlNotFoundException
 */
public class BusinessRuleException extends RuntimeException {
    /**
     * Constructs a new business rule exception with the specified detail message.
     *
     * @param message the detail message explaining the business rule violation
     */
    public BusinessRuleException(String message) {
        super(message);
    }
}
