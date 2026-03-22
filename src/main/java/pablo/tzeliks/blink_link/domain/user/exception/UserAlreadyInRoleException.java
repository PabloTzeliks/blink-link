package pablo.tzeliks.blink_link.domain.user.exception;

import pablo.tzeliks.blink_link.domain.common.exception.BusinessRuleException;

public class UserAlreadyInRoleException extends BusinessRuleException {
    public UserAlreadyInRoleException(String message) {
        super(message);
    }
}
