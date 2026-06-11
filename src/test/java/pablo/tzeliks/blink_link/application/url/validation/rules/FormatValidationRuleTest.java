package pablo.tzeliks.blink_link.application.url.validation.rules;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pablo.tzeliks.blink_link.application.url.exception.InvalidCustomCodeException;

import static org.junit.jupiter.api.Assertions.*;

class FormatValidationRuleTest {

    private FormatValidationRule rule;

    @BeforeEach
    void setUp() {
        rule = new FormatValidationRule();
    }

    @Test
    @DisplayName("Should pass when code has minimum length (4 chars)")
    void shouldReturnValidWhenMinLength() {
        assertDoesNotThrow(() -> rule.validate("abcd"));
    }

    @Test
    @DisplayName("Should pass when code has maximum length (20 chars)")
    void shouldReturnValidWhenMaxLength() {
        assertDoesNotThrow(() -> rule.validate("abcdefghij1234567890"));
    }

    @Test
    @DisplayName("Should pass when code contains hyphen in the middle")
    void shouldReturnValidWhenContainsHyphen() {
        assertDoesNotThrow(() -> rule.validate("my-link"));
    }

    @Test
    @DisplayName("Should pass when code contains underscore in the middle")
    void shouldReturnValidWhenContainsUnderscore() {
        assertDoesNotThrow(() -> rule.validate("my_link"));
    }

    @Test
    @DisplayName("Should throw exception when code is too short (3 chars)")
    void shouldThrowExceptionWhenTooShort() {
        assertThrows(InvalidCustomCodeException.class, () -> rule.validate("abc"));
    }

    @Test
    @DisplayName("Should throw exception when code is too long (21 chars)")
    void shouldThrowExceptionWhenTooLong() {
        assertThrows(InvalidCustomCodeException.class, () -> rule.validate("abcdefghij12345678901"));
    }

    @Test
    @DisplayName("Should throw exception when code starts with hyphen")
    void shouldThrowExceptionWhenStartsWithHyphen() {
        assertThrows(InvalidCustomCodeException.class, () -> rule.validate("-mylink"));
    }

    @Test
    @DisplayName("Should throw exception when code ends with hyphen")
    void shouldThrowExceptionWhenEndsWithHyphen() {
        assertThrows(InvalidCustomCodeException.class, () -> rule.validate("mylink-"));
    }

    @Test
    @DisplayName("Should throw exception when code starts with underscore")
    void shouldThrowExceptionWhenStartsWithUnderscore() {
        assertThrows(InvalidCustomCodeException.class, () -> rule.validate("_mylink"));
    }

    @Test
    @DisplayName("Should throw exception when code contains space")
    void shouldThrowExceptionWhenContainsSpace() {
        assertThrows(InvalidCustomCodeException.class, () -> rule.validate("my link"));
    }

    @Test
    @DisplayName("Should throw exception when code contains special character")
    void shouldThrowExceptionWhenContainsSpecialChar() {
        assertThrows(InvalidCustomCodeException.class, () -> rule.validate("my@link"));
    }

    @Test
    @DisplayName("Should throw exception when code is null")
    void shouldThrowExceptionWhenNull() {
        assertThrows(InvalidCustomCodeException.class, () -> rule.validate(null));
    }
}
