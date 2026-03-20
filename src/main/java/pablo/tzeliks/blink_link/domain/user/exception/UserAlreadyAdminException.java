package pablo.tzeliks.blink_link.domain.user.exception;

import pablo.tzeliks.blink_link.domain.common.exception.BusinessRuleException;

public class UserAlreadyAdminException extends BusinessRuleException {
    public UserAlreadyAdminException(String message) {
        super(message);
    }
}
