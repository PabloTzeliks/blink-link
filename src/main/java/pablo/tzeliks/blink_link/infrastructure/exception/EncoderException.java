package pablo.tzeliks.blink_link.infrastructure.exception;

/**
 * Exception thrown when URL encoding operations fail.
 * <p>
 * This exception indicates that the Base62 encoding process encountered an error.
 * Common scenarios include:
 * <ul>
 *   <li>Attempting to encode a {@code null} ID</li>
 *   <li>Attempting to encode a negative ID</li>
 *   <li>Configuration errors with the encoding character set</li>
 * </ul>
 * <p>
 * When this exception occurs during URL shortening, it is typically caught and
 * re-thrown as an {@link pablo.tzeliks.blink_link.domain.url.exception.InvalidUrlException}
 * to maintain clean separation between infrastructure and domain concerns. This
 * allows the domain layer to handle encoding failures as business rule violations
 * rather than infrastructure errors.
 *
 * @author Pablo Tzeliks
 * @version 2.0.0
 * @since 1.0.0
 * @see InfraestructureException
 * @see pablo.tzeliks.blink_link.infrastructure.encoding.Base62Encoder
 */
public class EncoderException extends InfraestructureException {
    /**
     * Constructs a new encoder exception with the specified detail message.
     *
     * @param message the detail message explaining why the encoding failed
     */
    public EncoderException(String message) {
        super(message);
    }
}
