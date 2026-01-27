package pablo.tzeliks.blink_link.domain.url.exception;

/**
 * Exception thrown when a URL lookup fails because the short code doesn't exist.
 * <p>
 * This exception is raised when attempting to resolve or access a short code
 * that is not present in the database. This typically occurs when:
 * <ul>
 *   <li>A user requests a short code that was never created</li>
 *   <li>A short code has been deleted or expired (if deletion is implemented)</li>
 *   <li>A user manually types an invalid short code</li>
 * </ul>
 * <p>
 * When caught by the global exception handler, this exception results in an
 * HTTP 404 (Not Found) response to the client, indicating that the requested
 * resource does not exist.
 * <p>
 * <b>User Experience:</b> This exception allows the application to provide
 * clear feedback to users that their requested short code is not valid, rather
 * than showing a generic error or allowing the application to fail.
 *
 * @author Pablo Tzeliks
 * @version 2.0.0
 * @since 1.0.0
 * @see BusinessRuleException
 */
public class UrlNotFoundException extends BusinessRuleException {
    /**
     * Constructs a new URL not found exception with the specified detail message.
     *
     * @param message the detail message explaining which URL was not found
     */
    public UrlNotFoundException(String message) {
        super(message);
    }
}
