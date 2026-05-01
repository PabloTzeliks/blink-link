package pablo.tzeliks.blink_link.application.url.validation;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CustomCodeValidator {

    private final List<CodeValidationRule> rules;

    public CustomCodeValidator(List<CodeValidationRule> rules) {
        this.rules = rules;
    }

    public void validate(String code) {
        rules.forEach(rule -> rule.validate(code));
    }
}
