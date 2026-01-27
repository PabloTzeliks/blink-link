package pablo.tzeliks.blink_link.infrastructure.encoding;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import pablo.tzeliks.blink_link.infrastructure.exception.EncoderException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Base62EncoderTest {

    private Base62Encoder encoder;

    private static final String TEST_BASE = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    @BeforeEach
    void setUp() {
        encoder = new Base62Encoder();

        // Simulates the @Value injection from .application
        ReflectionTestUtils.setField(encoder, "privateBase", TEST_BASE);

        encoder.init();
    }

    @Test
    @DisplayName("Should encode ID 0 to the first character of the base")
    void shouldEncodeZero() {
        String result = encoder.encode(0L);
        assertEquals("0", result);
    }

    @Test
    @DisplayName("Should encode ID 61 to the last character of the base")
    void shouldEncodeLastSingleDigit() {
        // Limit Check: The last ID before changing to 2 digits (on 62 base)
        // 0-9 (10) + a-z (26) + A-Z (26) = 62 characters. The index goes from 0 to 61.
        String result = encoder.encode(61L);
        assertEquals("Z", result);
    }

    @Test
    @DisplayName("Should encode ID 62 to two characters (10)")
    void shouldEncodeRollover() {
        // Rollover Check: 62 should transform to "10"
        // Math way:
        // 62 / 62 = 1 (remainder 0) -> char '0'
        // 1 / 62 = 0 (remainder 1) -> char '1'
        // Reversed String = "10"
        String result = encoder.encode(62L);
        assertEquals("10", result);
    }

    @Test
    @DisplayName("Should encode a large ID correctly")
    void shouldEncodeLargeId() {
        // Consistency Check
        // ID 1000 on Base62 should be "g8"
        // 1000 / 62 = 16 (remainder 8) -> char '8'
        // 16 / 62 = 0 (remainder 16) -> char 'g' (0-9 are 10, a-f are 6, g is the 16º index)
        // Reversed: "g8"
        String result = encoder.encode(1000L);
        assertEquals("g8", result);
    }

    @Test
    @DisplayName("Should throw EncoderException when ID is null")
    void shouldThrowExceptionWhenIdIsNull() {
        EncoderException ex = assertThrows(EncoderException.class, () -> encoder.encode(null));
        assertEquals("ID cannot be null", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw EncoderException when ID is negative")
    void shouldThrowExceptionWhenIdIsNegative() {
        EncoderException ex = assertThrows(EncoderException.class, () -> encoder.encode(-1L));
        assertEquals("ID cannot be negative", ex.getMessage());
    }
}