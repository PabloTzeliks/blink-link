package pablo.tzeliks.blink_link.domain.user.exception;

import pablo.tzeliks.blink_link.domain.common.exception.ResourceNotFoundException;

public class UserNotFoundException extends ResourceNotFoundException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
