package pablo.tzeliks.blink_link.application.url.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pablo.tzeliks.blink_link.application.url.exception.InvalidCustomCodeException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomCodeValidatorTest {

    @Mock
    private CodeValidationRule rule1;

    @Mock
    private CodeValidationRule rule2;

    private CustomCodeValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CustomCodeValidator(List.of(rule1, rule2));
    }

    @Test
    @DisplayName("Should pass when all rules pass")
    void should_Pass_When_AllRulesPass() {
        assertDoesNotThrow(() -> validator.validate("mycode"));
        verify(rule1).validate("mycode");
        verify(rule2).validate("mycode");
    }

    @Test
    @DisplayName("Should fail fast when first rule fails")
    void should_FailFast_When_FirstRuleFails() {
        doThrow(new InvalidCustomCodeException("Invalid format")).when(rule1).validate("mycode");

        assertThrows(InvalidCustomCodeException.class, () -> validator.validate("mycode"));

        verify(rule1).validate("mycode");
        verify(rule2, never()).validate(any());
    }

    @Test
    @DisplayName("Should fail when second rule fails")
    void should_Fail_When_SecondRuleFails() {
        doThrow(new InvalidCustomCodeException("Reserved word")).when(rule2).validate("mycode");

        assertThrows(InvalidCustomCodeException.class, () -> validator.validate("mycode"));

        verify(rule1).validate("mycode");
        verify(rule2).validate("mycode");
    }
}
