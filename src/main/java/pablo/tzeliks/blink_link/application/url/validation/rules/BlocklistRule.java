package pablo.tzeliks.blink_link.application.url.validation.rules;

import org.ahocorasick.trie.Trie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import pablo.tzeliks.blink_link.application.url.exception.InvalidCustomCodeException;
import pablo.tzeliks.blink_link.application.url.validation.CodeValidationRule;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@Component
@Order(3)
public class BlocklistRule implements CodeValidationRule {

    private final Trie trie;

    public BlocklistRule(
            @Value("classpath:validation/blocked-terms.txt") Resource file) throws IOException {

        List<String> terms = Files.readAllLines(file.getFile().toPath())
                .stream()
                .map(String::toLowerCase)
                .filter(line -> !line.isBlank())
                .toList();

        this.trie = Trie.builder()
                .ignoreCase()
                .addKeywords(terms)
                .build();
    }

    @Override
    public void validate(String code) {
        if (!trie.parseText(code.toLowerCase()).isEmpty()) {
            throw new InvalidCustomCodeException(
                    "Custom code contains prohibited content."
            );
        }
    }
}
