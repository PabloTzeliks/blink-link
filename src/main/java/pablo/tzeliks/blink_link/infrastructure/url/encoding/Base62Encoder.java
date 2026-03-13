package pablo.tzeliks.blink_link.infrastructure.url.encoding;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import pablo.tzeliks.blink_link.domain.url.ports.ShortenerPort;
import pablo.tzeliks.blink_link.infrastructure.url.exception.EncoderException;

/**
 * Base62 encoder implementation for generating short codes from numeric IDs.
 * <p>
 * This component implements the {@link ShortenerPort} interface to provide Base62
 * encoding functionality. Base62 encoding converts numeric IDs into compact,
 * URL-safe alphanumeric strings using a custom character set.
 * <p>
 * <b>Why Base62?</b>
 * <ul>
 *   <li><b>Compact:</b> Produces shorter strings than decimal (base 10) or hexadecimal (base 16)</li>
 *   <li><b>URL-Safe:</b> Uses only alphanumeric characters (a-z, A-Z, 0-9) without special characters</li>
 *   <li><b>Human-Friendly:</b> Avoids confusing characters like 0/O, 1/l/I when configured properly</li>
 *   <li><b>Reversible:</b> Can be decoded back to the original ID if needed</li>
 * </ul>
 * <p>
 * <b>Algorithm:</b>
 * <p>
 * The encoding process works as follows:
 * <ol>
 *   <li>Take the numeric ID (e.g., 12345)</li>
 *   <li>Repeatedly divide by the base (62) and take the remainder</li>
 *   <li>Map each remainder to a character in the custom character set</li>
 *   <li>Reverse the resulting characters to get the final short code</li>
 * </ol>
 * <p>
 * <b>Example:</b> ID {@code 123456} → Short Code {@code "W7E"}
 * <p>
 * <b>Character Set:</b>
 * <p>
 * The character set is configurable via the {@code blink-link.secret-key} property.
 * A standard Base62 alphabet uses: {@code 0-9, a-z, A-Z} (62 characters total).
 * The secret key allows for a custom ordering or character set, adding an additional
 * layer of unpredictability.
 * <p>
 * <b>Error Handling:</b>
 * <ul>
 *   <li>Throws {@link EncoderException} if the ID is {@code null}</li>
 *   <li>Throws {@link EncoderException} if the ID is negative</li>
 *   <li>Returns the first character of the alphabet for ID {@code 0}</li>
 * </ul>
 *
 * @author Pablo Tzeliks
 * @version 2.0.0
 * @since 1.0.0
 * @see ShortenerPort
 */
@Component
@Primary
public class Base62Encoder implements ShortenerPort {

    /**
     * The custom character set used for Base62 encoding.
     * <p>
     * This value is injected from the application configuration property
     * {@code blink-link.secret-key}. The length of this string determines the
     * actual base of the encoding (typically 62 characters for standard Base62).
     * <p>
     * A custom character set provides:
     * <ul>
     *   <li>Flexibility in character ordering</li>
     *   <li>Ability to exclude confusing characters (e.g., 0, O, 1, l, I)</li>
     *   <li>Additional security through obscurity (non-standard ordering)</li>
     * </ul>
     */
    @Value(value = "${blink-link.secret-key}")
    private String privateBase;

    /**
     * The calculated base for encoding operations.
     * <p>
     * This value is set during initialization and equals the length of the
     * {@code privateBase} string. For standard Base62, this will be 62.
     */
    private int base;

    /**
     * Initializes the encoder by calculating the base from the character set length.
     * <p>
     * This method is automatically invoked after dependency injection is complete
     * ({@code @PostConstruct}). It sets the {@code base} field to the length of
     * the configured character set, which is used in all encoding operations.
     */
    @PostConstruct
    public void init() {
        this.base = privateBase.length();
    }

    /**
     * Encodes a numeric ID into a Base62 short code.
     * <p>
     * This method implements the Base62 encoding algorithm:
     * <ol>
     *   <li>Validates the input ID (not null, not negative)</li>
     *   <li>Handles the special case of ID = 0</li>
     *   <li>Repeatedly divides the ID by the base, mapping remainders to characters</li>
     *   <li>Reverses the result to get the final short code</li>
     * </ol>
     * <p>
     * <b>Time Complexity:</b> O(log_base(id)), typically O(log₆₂(id))
     * <p>
     * <b>Examples:</b>
     * <ul>
     *   <li>ID {@code 0} → First character of the alphabet</li>
     *   <li>ID {@code 61} → Second character of the alphabet</li>
     *   <li>ID {@code 62} → Two characters (second char + first char)</li>
     *   <li>ID {@code 123456} → Short code like "W7E" (depends on character set)</li>
     * </ul>
     *
     * @param id the numeric identifier to encode; must be non-null and non-negative
     * @return a Base62-encoded short code string
     * @throws EncoderException if the ID is {@code null}
     * @throws EncoderException if the ID is negative
     */
    @Override
    public String encode(Long id) {

        if (id == null) { throw new EncoderException("ID cannot be null"); }

        if (id < 0) { throw new EncoderException("ID cannot be negative"); }

        if (id == 0) { return String.valueOf(privateBase.charAt(0)); }

        StringBuilder encoded = new StringBuilder();

        while (id > 0) {
            int remainder = (int) (id % base);
            encoded.append(privateBase.charAt(remainder));
            id /= base;
        }

        return encoded.reverse().toString();
    }
}