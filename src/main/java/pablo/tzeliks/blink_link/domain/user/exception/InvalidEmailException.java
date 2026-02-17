package pablo.tzeliks.blink_link.domain.user.exception;

import pablo.tzeliks.blink_link.domain.common.exception.DomainException;

public class InvalidEmailException extends DomainException {
    public InvalidEmailException(String message) {
        super(message);
    }
}
