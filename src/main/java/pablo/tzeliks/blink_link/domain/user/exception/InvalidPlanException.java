package pablo.tzeliks.blink_link.domain.user.exception;

import pablo.tzeliks.blink_link.domain.common.exception.InvalidResourceException;

public class InvalidPlanException extends InvalidResourceException {
    public InvalidPlanException(String message) {
        super(message);
    }
}
