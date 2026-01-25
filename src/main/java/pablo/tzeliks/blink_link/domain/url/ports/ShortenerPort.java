package pablo.tzeliks.blink_link.domain.url.ports;

import pablo.tzeliks.blink_link.infrastructure.encoding.Base62Encoder;

/**
 * TODO : new Docs
 *
 * @author Pablo Tzeliks
 * @version 2.0.0
 * @since 1.0.0
 * @see Base62Encoder
 */
public interface ShortenerPort {

    /**
     * Encodes a numeric ID into a short code string.
     * <p>
     * This method transforms a database-generated Long ID into a shorter, more
     * user-friendly string representation suitable for use in URLs.
     * <p>
     * Implementations should guarantee:
     * <ul>
     *   <li>Uniqueness: Different IDs produce different short codes</li>
     *   <li>Consistency: Same ID always produces the same short code</li>
     *   <li>Reversibility: The short code can be decoded back to the original ID</li>
     *   <li>URL-safety: Generated codes contain only URL-safe characters</li>
     * </ul>
     *
     * @param id the numeric identifier to encode; typically a database primary key
     * @return a short code string representing the encoded ID
     * @throws IllegalArgumentException if the ID is null or invalid
     */
    String encode(Long id);
}
