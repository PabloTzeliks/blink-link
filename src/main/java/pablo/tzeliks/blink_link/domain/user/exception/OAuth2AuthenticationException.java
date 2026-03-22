package pablo.tzeliks.blink_link.domain.user.exception;

import pablo.tzeliks.blink_link.domain.common.exception.AuthenticationException;

public class OAuth2AuthenticationException extends AuthenticationException {
    public OAuth2AuthenticationException(String message) {
        super(message);
    }
}
