package pablo.tzeliks.blink_link.domain.user.exception;

import pablo.tzeliks.blink_link.domain.common.exception.DomainException;

public class InvalidPasswordException extends DomainException {
    public InvalidPasswordException(String message) {
        super(message);
    }
}
