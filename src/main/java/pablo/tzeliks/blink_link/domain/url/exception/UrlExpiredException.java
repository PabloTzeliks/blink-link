package pablo.tzeliks.blink_link.domain.url.exception;

import pablo.tzeliks.blink_link.domain.common.exception.BusinessRuleException;

public class UrlExpiredException extends BusinessRuleException {
    public UrlExpiredException(String message) {
        super(message);
    }
}
