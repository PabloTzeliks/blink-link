package pablo.tzeliks.blink_link.application.url.validation.rules;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import pablo.tzeliks.blink_link.application.url.exception.InvalidCustomCodeException;
import pablo.tzeliks.blink_link.application.url.validation.CodeValidationRule;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Order(2)
public class ReservedWordsRule implements CodeValidationRule {

    private final Set<String> reserved;

    public ReservedWordsRule(Set<String> reserved) {
        this.reserved = reserved;
    }

    public ReservedWordsRule(
            @Value("classpath:validation/reserved-codes.txt") Resource file) throws IOException {

        this.reserved = Files.readAllLines(file.getFile().toPath())
                .stream()
                .map(String::toLowerCase)
                .filter(line -> !line.isBlank())
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public void validate(String code) {
        if (reserved.contains(code.toLowerCase())) {
            throw new InvalidCustomCodeException(
                    "Code '" + code + "' is reserved and cannot be used."
            );
        }
    }
}
