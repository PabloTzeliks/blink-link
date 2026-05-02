package pablo.tzeliks.blink_link.application.url.validation.rules;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import pablo.tzeliks.blink_link.application.url.exception.InvalidCustomCodeException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class BlocklistRuleTest {

    private BlocklistRule rule;

    @BeforeEach
    void setUp() throws IOException {
        rule = new BlocklistRule(new ClassPathResource("validation/blocked-terms.txt"));
    }

    @Test
    @DisplayName("Should throw exception when code contains a blocked term")
    void should_ThrowException_When_ContainsBlockedTerm() {
        assertThrows(InvalidCustomCodeException.class, () -> rule.validate("porn"));
    }

    @Test
    @DisplayName("Should pass when code is clean")
    void should_Pass_When_CleanCode() {
        assertDoesNotThrow(() -> rule.validate("promo25"));
    }

    @Test
    @DisplayName("Should pass when code is clean with hyphen")
    void should_Pass_When_CleanCodeWithHyphen() {
        assertDoesNotThrow(() -> rule.validate("best-deal"));
    }
}
