package pablo.tzeliks.blink_link.domain.url.exception;

import pablo.tzeliks.blink_link.domain.common.exception.BusinessRuleException;

/**
 * Exception thrown when URL validation fails according to business rules.
 * <p>
 * This exception indicates that a URL does not meet the application's business
 * requirements. Common validation failures include:
 * <ul>
 *   <li>URL is {@code null}, empty, or blank</li>
 *   <li>URL does not start with "http://" or "https://"</li>
 *   <li>URL format is invalid</li>
 *   <li>Short code is {@code null}, empty, or blank</li>
 *   <li>Encoding errors when generating short codes</li>
 * </ul>
 * <p>
 * When caught by the global exception handler, this exception typically results
 * in an HTTP 400 (Bad Request) response to the client, with a detailed error message
 * explaining the validation failure.
 *
 * @author Pablo Tzeliks
 * @version 2.0.0
 * @since 1.0.0
 * @see BusinessRuleException
 */
public class InvalidUrlException extends BusinessRuleException {
    /**
     * Constructs a new invalid URL exception with the specified detail message.
     *
     * @param message the detail message explaining why the URL is invalid
     */
    public InvalidUrlException(String message) {
        super(message);
    }
}
