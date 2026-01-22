package pablo.tzeliks.blink_link.domain.url.ports;

import pablo.tzeliks.blink_link.infraestructure.encoding.Base62Encoder;

/**
 * Interface defining the contract for URL shortening encoding and decoding operations.
 * <p>
 * This interface exists to support the <b>Interface Segregation Principle (ISP)</b>
 * and <b>Dependency Inversion Principle (DIP)</b> from SOLID design principles.
 * By programming to an interface rather than a concrete implementation, we achieve:
 * <ul>
 *   <li><b>Flexibility:</b> Multiple encoding strategies can be implemented and swapped
 *       without modifying dependent code</li>
 *   <li><b>Testability:</b> Mock implementations can be easily created for unit testing</li>
 *   <li><b>Extensibility:</b> New encoding algorithms can be added without breaking
 *       existing functionality</li>
 *   <li><b>Decoupling:</b> Service layer depends on abstraction, not concrete classes</li>
 * </ul>
 * <p>
 * <b>Why an Interface?</b> The decision to use an interface provides architectural flexibility.
 * For example, the current implementation uses Base62 encoding, but we could easily add
 * alternatives like Base58, UUID-based, or custom algorithms by simply implementing this
 * interface, without touching the service layer code.
 * <p>
 * Implementations of this interface should ensure bidirectional consistency: encoding
 * an ID and then decoding the result should return the original ID.
 *
 * @author Pablo Tzeliks
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

    /**
     * Decodes a short code string back into its original numeric ID.
     * <p>
     * This method performs the reverse operation of {@link #encode(Long)}, converting
     * a short code back to the database ID it represents.
     * <p>
     * Implementations should ensure that {@code decode(encode(id))} returns the original ID,
     * maintaining bidirectional consistency between encoding and decoding operations.
     *
     * @param shortCode the short code to decode; must be a valid code produced by encode()
     * @return the original numeric ID that was encoded
     * @throws IllegalArgumentException if the short code is null, empty, or contains
     *         invalid characters not present in the encoding alphabet
     */
    Long decode(String shortCode);
}
