package pablo.tzeliks.blink_link.domain.user.exception;

import pablo.tzeliks.blink_link.domain.common.exception.AuthenticationException;

public class InvalidEmailException extends AuthenticationException {
    public InvalidEmailException(String message) {
        super(message);
    }
}
