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
    void shouldThrowExceptionWhenReservedWordExactMatch() {
        assertThrows(InvalidCustomCodeException.class, () -> rule.validate("api"));
    }

    @Test
    @DisplayName("Should throw exception when code is a reserved word - case insensitive")
    void shouldThrowExceptionWhenReservedWordCaseInsensitive() {
        assertThrows(InvalidCustomCodeException.class, () -> rule.validate("API"));
    }

    @Test
    @DisplayName("Should throw exception when code is 'admin'")
    void shouldThrowExceptionWhenReservedWordAdmin() {
        assertThrows(InvalidCustomCodeException.class, () -> rule.validate("admin"));
    }

    @Test
    @DisplayName("Should pass when code is clean")
    void shouldPassWhenCleanCode() {
        assertDoesNotThrow(() -> rule.validate("meulink"));
    }

    @Test
    @DisplayName("Should pass when code is clean with numbers")
    void shouldPassWhenCleanCodeWithNumbers() {
        assertDoesNotThrow(() -> rule.validate("promo25"));
    }
}
