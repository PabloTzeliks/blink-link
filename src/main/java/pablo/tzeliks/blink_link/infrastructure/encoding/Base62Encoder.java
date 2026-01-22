package pablo.tzeliks.blink_link.infrastructure.encoding;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import pablo.tzeliks.blink_link.domain.url.ports.ShortenerPort;

/**
 * Base62 encoder implementation for converting numeric IDs to short URL-safe codes.
 * <p>
 * This class implements the {@link ShortenerPort} interface using Base62 encoding,
 * a mathematical conversion algorithm that transforms Base 10 (decimal) numbers
 * into Base 62 representation. Base62 uses 62 characters: [0-9, a-z, A-Z], which
 * provides a good balance between:
 * <ul>
 *   <li>Compactness: Shorter codes than decimal or hexadecimal</li>
 *   <li>Readability: Uses familiar alphanumeric characters</li>
 *   <li>URL-safety: All characters are safe for use in URLs without encoding</li>
 * </ul>
 * <p>
 * <b>Why Base62?</b> For a URL shortener, Base62 is ideal because it:
 * <ul>
 *   <li>Produces shorter codes than lower bases (e.g., ID 123456 → "w7e" in Base62)</li>
 *   <li>Avoids special characters that require URL encoding (unlike Base64's +/=)</li>
 *   <li>Remains human-readable and easy to communicate verbally or in print</li>
 *   <li>Maximizes the number of unique codes per character length</li>
 * </ul>
 * <p>
 * <b>Mathematical Foundation:</b> The encoding process uses modular arithmetic:
 * <pre>
 * For ID = 125:
 * 125 ÷ 62 = 2 remainder 1 → charAt(1)
 * 2 ÷ 62 = 0 remainder 2 → charAt(2)
 * Result (reversed): charAt(2) + charAt(1)
 * </pre>
 * <p>
 * The alphabet is configurable via {@code blink-link.secret-key} property, allowing
 * custom character sets for additional security or customization.
 * <p>
 * This implementation is marked as {@code @Primary} to serve as the default
 * {@link ShortenerPort} implementation when multiple implementations exist.
 *
 * @author Pablo Tzeliks
 * @since 1.0.0
 */
@Component
@Primary
public class Base62Encoder implements ShortenerPort {

    @Value(value = "${blink-link.secret-key}")
    private String privateBase;

    /**
     * Encodes a numeric ID into a Base62 string representation.
     * <p>
     * This method performs a mathematical conversion from Base 10 (decimal) to Base 62
     * using the configured character alphabet. The algorithm works through repeated
     * division by the base (62), collecting remainders which correspond to positions
     * in the character set.
     * <p>
     * <b>Algorithm Steps:</b>
     * <ol>
     *   <li>Validate input: throw exception if ID is null</li>
     *   <li>Handle special case: ID of 0 returns first character of alphabet</li>
     *   <li>Iteratively divide ID by base size, collecting remainders</li>
     *   <li>Map each remainder to its corresponding character in the alphabet</li>
     *   <li>Reverse the result (mathematical conversion produces digits in reverse order)</li>
     * </ol>
     * <p>
     * <b>Example:</b> For ID 3844 with standard Base62 alphabet [0-9a-zA-Z]:
     * <pre>
     * 3844 % 62 = 0 → '0'
     * 62 % 62 = 0 → '0'
     * 1 % 62 = 1 → '1'
     * Result: "100" (in Base62)
     * </pre>
     *
     * @param id the numeric identifier to encode; must not be null
     * @return a Base62-encoded string representing the ID
     * @throws IllegalArgumentException if the ID is null
     */
    @Override
    public String encode(Long id) {

        if (id == null) throw new IllegalArgumentException("ID cannot be Null");

        if (id == 0) return String.valueOf(privateBase.charAt(0));

        StringBuilder encoded = new StringBuilder();
        long base = privateBase.length();

        while (id > 0) {
            int remainder = (int) (id % base);
            encoded.append(privateBase.charAt((remainder)));

            id /= base;
        }

        return encoded.reverse().toString();
    }

    /**
     * Decodes a Base62 string back into its original numeric ID.
     * <p>
     * This method reverses the encoding process by converting a Base62 string back
     * to its decimal (Base 10) representation. It processes each character from left
     * to right, treating the string as a number in Base62 positional notation.
     * <p>
     * <b>Algorithm Steps:</b>
     * <ol>
     *   <li>Validate input: throw exception if short code is null or empty</li>
     *   <li>For each character in the short code:
     *     <ul>
     *       <li>Find its position (index) in the alphabet</li>
     *       <li>Validate character exists in alphabet</li>
     *       <li>Multiply running total by base and add character's index</li>
     *     </ul>
     *   </li>
     * </ol>
     * <p>
     * <b>Mathematical Formula:</b> For a string "abc" in Base62:
     * <pre>
     * result = (a × 62²) + (b × 62¹) + (c × 62⁰)
     * </pre>
     * Where a, b, c are the indices of characters in the alphabet.
     * <p>
     * This method ensures bidirectional consistency: {@code decode(encode(id))}
     * always returns the original ID value.
     *
     * @param shortCode the Base62 string to decode; must not be null or empty
     * @return the original numeric ID that was encoded
     * @throws IllegalArgumentException if the short code is null, empty, or contains
     *         characters not present in the configured alphabet
     */
    @Override
    public Long decode(String shortCode) {

        if (shortCode == null || shortCode.isEmpty()) throw new IllegalArgumentException("Short Code cannot be empty");

        long decoded = 0;
        long base = privateBase.length();

        for (char c : shortCode.toCharArray()) {
            int index = privateBase.indexOf(c);

            if (index == -1) throw new IllegalArgumentException("Character '" + c + "' not found in the Base");

            decoded = decoded * base + index;
        }

        return decoded;
    }
}
