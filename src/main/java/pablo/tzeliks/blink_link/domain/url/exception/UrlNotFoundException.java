package pablo.tzeliks.blink_link.domain.url.exception;

import pablo.tzeliks.blink_link.domain.common.exception.ResourceNotFoundException;

/**
 * @author Pablo Tzeliks
 * @version 2.0.0
 * @since 1.0.0
 */
public class UrlNotFoundException extends ResourceNotFoundException {
    public UrlNotFoundException(String message) {
        super(message);
    }
}
