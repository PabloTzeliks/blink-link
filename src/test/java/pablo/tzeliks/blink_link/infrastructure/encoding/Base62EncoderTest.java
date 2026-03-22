package pablo.tzeliks.blink_link.infrastructure.encoding;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import pablo.tzeliks.blink_link.infrastructure.url.exception.EncoderException;
import pablo.tzeliks.blink_link.infrastructure.url.encoding.Base62Encoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for the Base62 encoding algorithm.
 * <p>
 * This test class validates the {@link Base62Encoder} implementation, testing
 * the mathematical correctness of the Base62 encoding algorithm and its edge cases.
 * <p>
 * <b>Test Strategy:</b>
 * <p>
 * These are pure unit tests that:
 * <ul>
 *   <li>Test the encoder in isolation without Spring context</li>
 *   <li>Use reflection to inject test configuration (character set)</li>
 *   <li>Validate encoding for boundary values (0, 61, 62)</li>
 *   <li>Verify mathematical correctness for various IDs</li>
 *   <li>Test error handling for invalid inputs</li>
 * </ul>
 * <p>
 * <b>Test Coverage:</b>
 * <ul>
 *   <li>Zero: Special case returning first character</li>
 *   <li>Single digit: IDs 0-61 map to single characters</li>
 *   <li>Rollover: ID 62 produces two characters</li>
 *   <li>Large IDs: Correctness for multi-character codes</li>
 *   <li>Null validation: Rejection of null IDs</li>
 *   <li>Negative validation: Rejection of negative IDs</li>
 * </ul>
 * <p>
 * <b>Base62 Character Set:</b>
 * <p>
 * The test uses the standard Base62 alphabet: {@code 0-9, a-z, A-Z} (62 characters).
 * This is injected via reflection to simulate the {@code @Value} annotation behavior
 * without requiring the full Spring context.
 *
 * @author Pablo Tzeliks
 * @version 2.0.0
 * @since 1.0.0
 * @see Base62Encoder
 */
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

    /**
     * Unit Test: Verifies encoding of zero.
     * <p>
     * <b>Scenario:</b> Edge Case - ID is 0
     * <p>
     * <b>Given:</b> ID value of 0
     * <br><b>When:</b> encode() is called
     * <br><b>Then:</b> Returns the first character of the Base62 alphabet
     * <p>
     * <b>Expected Result:</b> "0" (the first character in the test alphabet)
     * <p>
     * This tests the special case handling for zero, which is handled
     * separately in the algorithm to avoid an empty result.
     */
    @Test
    @DisplayName("Should encode ID 0 to the first character of the base")
    void shouldEncodeZero() {
        String result = encoder.encode(0L);
        assertEquals("0", result);
    }

    /**
     * Unit Test: Verifies encoding of the maximum single-digit value.
     * <p>
     * <b>Scenario:</b> Boundary Value - Largest single-character code
     * <p>
     * <b>Given:</b> ID value of 61 (the last index in a 62-character alphabet)
     * <br><b>When:</b> encode() is called
     * <br><b>Then:</b> Returns the last character of the Base62 alphabet
     * <p>
     * <b>Expected Result:</b> "Z" (the 62nd character: 0-9=10, a-z=26, A-Z=26, total 62)
     * <p>
     * This tests the boundary between single-character and multi-character codes.
     * ID 61 is the last value that produces a single character; ID 62 will roll over
     * to two characters.
     */
    @Test
    @DisplayName("Should encode ID 61 to the last character of the base")
    void shouldEncodeLastSingleDigit() {
        // Limit Check: The last ID before changing to 2 digits (on 62 base)
        // 0-9 (10) + a-z (26) + A-Z (26) = 62 characters. The index goes from 0 to 61.
        String result = encoder.encode(61L);
        assertEquals("Z", result);
    }

    /**
     * Unit Test: Verifies encoding rollover to two characters.
     * <p>
     * <b>Scenario:</b> Boundary Value - First two-character code
     * <p>
     * <b>Given:</b> ID value of 62 (one beyond the single-character range)
     * <br><b>When:</b> encode() is called
     * <br><b>Then:</b> Returns a two-character code
     * <p>
     * <b>Mathematical Breakdown:</b>
     * <pre>
     * 62 ÷ 62 = 1 remainder 0 → character at index 0 = '0'
     *  1 ÷ 62 = 0 remainder 1 → character at index 1 = '1'
     * Reversed: "10"
     * </pre>
     * <p>
     * <b>Expected Result:</b> "10"
     * <p>
     * This test validates the rollover behavior where the algorithm starts
     * producing multi-character codes, analogous to how decimal counting
     * goes from 9 to 10.
     */
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

    /**
     * Unit Test: Verifies encoding correctness for a large ID.
     * <p>
     * <b>Scenario:</b> Typical Use Case - Multi-character encoding
     * <p>
     * <b>Given:</b> ID value of 1000
     * <br><b>When:</b> encode() is called
     * <br><b>Then:</b> Returns the correct two-character Base62 code
     * <p>
     * <b>Mathematical Breakdown:</b>
     * <pre>
     * 1000 ÷ 62 = 16 remainder 8 → character at index 8 = '8'
     *   16 ÷ 62 =  0 remainder 16 → character at index 16 = 'g' (0-9=10, a-f=6, g=16)
     * Reversed: "g8"
     * </pre>
     * <p>
     * <b>Expected Result:</b> "g8"
     * <p>
     * This test validates the algorithm's correctness for realistic ID values
     * that would be encountered in production use, ensuring the mathematical
     * operations produce the expected Base62 string.
     */
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

    /**
     * Unit Test: Verifies null ID validation.
     * <p>
     * <b>Scenario:</b> Validation Error - Null input
     * <p>
     * <b>Given:</b> A null ID value
     * <br><b>When:</b> encode() is called
     * <br><b>Then:</b> EncoderException is thrown with descriptive message
     * <p>
     * <b>Expected Exception:</b> EncoderException with message "ID cannot be null"
     * <p>
     * This test validates the fail-fast validation that prevents
     * NullPointerException from occurring during the encoding process.
     */
    @Test
    @DisplayName("Should throw EncoderException when ID is null")
    void shouldThrowExceptionWhenIdIsNull() {
        EncoderException ex = assertThrows(EncoderException.class, () -> encoder.encode(null));
        assertEquals("ID cannot be null", ex.getMessage());
    }

    /**
     * Unit Test: Verifies negative ID validation.
     * <p>
     * <b>Scenario:</b> Validation Error - Negative input
     * <p>
     * <b>Given:</b> A negative ID value (-1)
     * <br><b>When:</b> encode() is called
     * <br><b>Then:</b> EncoderException is thrown with descriptive message
     * <p>
     * <b>Expected Exception:</b> EncoderException with message "ID cannot be negative"
     * <p>
     * This test validates the business rule that IDs must be non-negative,
     * preventing invalid encoding operations and potential infinite loops
     * in the encoding algorithm.
     */
    @Test
    @DisplayName("Should throw EncoderException when ID is negative")
    void shouldThrowExceptionWhenIdIsNegative() {
        EncoderException ex = assertThrows(EncoderException.class, () -> encoder.encode(-1L));
        assertEquals("ID cannot be negative", ex.getMessage());
    }
}