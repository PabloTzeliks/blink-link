package pablo.tzeliks.blink_link.application.url.validation;

import pablo.tzeliks.blink_link.application.url.exception.InvalidCustomCodeException;

public interface CodeValidationRule {

        boolean validate(String code) throws InvalidCustomCodeException;
}
