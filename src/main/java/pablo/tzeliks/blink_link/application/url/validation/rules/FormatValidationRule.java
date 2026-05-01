package pablo.tzeliks.blink_link.application.url.validation.rules;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pablo.tzeliks.blink_link.application.url.exception.InvalidCustomCodeException;
import pablo.tzeliks.blink_link.application.url.validation.CodeValidationRule;

import java.util.regex.Pattern;

@Component
@Order(1)
public class FormatValidationRule implements CodeValidationRule {

    private static final Pattern VALID_CODE =
            Pattern.compile("^[a-zA-Z0-9][a-zA-Z0-9_-]{2,5}[a-zA-Z0-9]$");

    @Override
    public void validate(String code) {
        throw new InvalidCustomCodeException(
                "Custom code must be 4–7 characters, alphanumeric with hyphens or underscores only, " +
                        "and cannot start or end with a hyphen or underscore."
        );
    }
}
