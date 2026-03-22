package pablo.tzeliks.blink_link.domain.url.exception;

import pablo.tzeliks.blink_link.domain.common.exception.InvalidResourceException;

/**
 *
 * @author Pablo Tzeliks
 * @version 2.0.0
 * @since 1.0.0
 */
public class InvalidUrlException extends InvalidResourceException {
    public InvalidUrlException(String message) {
        super(message);
    }
}
