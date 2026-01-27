package pablo.tzeliks.blink_link.infrastructure.exception;

/**
 * Base exception class for infrastructure-level errors.
 * <p>
 * This exception serves as the parent class for all infrastructure-related exceptions
 * in the application. It represents failures that occur in the infrastructure layer,
 * such as encoding errors, database connection issues, or external service failures.
 * <p>
 * By extending {@link RuntimeException}, this exception is unchecked, allowing it to
 * propagate without requiring explicit handling at every level. Infrastructure exceptions
 * are typically handled at boundary layers (e.g., controllers) where they can be
 * converted into appropriate HTTP responses.
 * <p>
 * <b>Subclasses:</b>
 * <ul>
 *   <li>{@link EncoderException} - Thrown when encoding operations fail</li>
 * </ul>
 *
 * @author Pablo Tzeliks
 * @version 2.0.0
 * @since 1.0.0
 * @see EncoderException
 */
public class InfraestructureException extends RuntimeException {
    /**
     * Constructs a new infrastructure exception with the specified detail message.
     *
     * @param message the detail message explaining the infrastructure failure
     */
    public InfraestructureException(String message) {
        super(message);
    }
}
