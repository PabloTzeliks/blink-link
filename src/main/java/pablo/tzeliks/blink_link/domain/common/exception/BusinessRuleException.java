package pablo.tzeliks.blink_link.domain.common.exception;

import pablo.tzeliks.blink_link.domain.url.exception.InvalidUrlException;
import pablo.tzeliks.blink_link.domain.url.exception.UrlNotFoundException;

/**
 *
 * @author Pablo Tzeliks
 * @version 2.0.0
 * @since 1.0.0
 */
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
