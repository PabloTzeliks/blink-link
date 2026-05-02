package pablo.tzeliks.blink_link.application.url.validation.rules;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import pablo.tzeliks.blink_link.application.url.exception.InvalidCustomCodeException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ReservedWordsRuleTest {

    private ReservedWordsRule rule;

    @BeforeEach
    void setUp() throws IOException {
        rule = new ReservedWordsRule(new ClassPathResource("validation/reserved-codes.txt"));
    }

    @Test
    @DisplayName("Should throw exception when code is a reserved word - exact match")
    void should_ThrowException_When_ReservedWord_ExactMatch() {
        assertThrows(InvalidCustomCodeException.class, () -> rule.validate("api"));
    }

    @Test
    @DisplayName("Should throw exception when code is a reserved word - case insensitive")
    void should_ThrowException_When_ReservedWord_CaseInsensitive() {
        assertThrows(InvalidCustomCodeException.class, () -> rule.validate("API"));
    }

    @Test
    @DisplayName("Should throw exception when code is 'admin'")
    void should_ThrowException_When_ReservedWord_Admin() {
        assertThrows(InvalidCustomCodeException.class, () -> rule.validate("admin"));
    }

    @Test
    @DisplayName("Should pass when code is clean")
    void should_Pass_When_CleanCode() {
        assertDoesNotThrow(() -> rule.validate("meulink"));
    }

    @Test
    @DisplayName("Should pass when code is clean with numbers")
    void should_Pass_When_CleanCodeWithNumbers() {
        assertDoesNotThrow(() -> rule.validate("promo25"));
    }
}
