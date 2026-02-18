package pablo.tzeliks.blink_link.domain.url.ports;

import pablo.tzeliks.blink_link.infrastructure.url.encoding.Base62Encoder;

/**
 * Port interface for URL shortening operations in the hexagonal architecture.
 * <p>
 * This interface defines the contract for encoding numeric IDs into short codes.
 * It represents an output port in the hexagonal (ports and adapters) architecture,
 * allowing the domain/application layer to depend on an abstraction rather than
 * a concrete implementation.
 * <p>
 * <b>Hexagonal Architecture:</b>
 * <p>
 * By defining this port interface in the domain layer, we achieve:
 * <ul>
 *   <li><b>Dependency Inversion:</b> The domain depends on abstractions, not implementations</li>
 *   <li><b>Testability:</b> Easy to mock or stub for unit testing</li>
 *   <li><b>Flexibility:</b> Implementation can be swapped (e.g., Base62, Base58, UUID)</li>
 *   <li><b>Separation of Concerns:</b> Domain logic is isolated from infrastructure details</li>
 * </ul>
 * <p>
 * <b>Implementation:</b>
 * <p>
 * The primary implementation is {@link Base62Encoder}, which provides Base62
 * encoding functionality. Additional implementations can be created without
 * modifying the domain or application layers.
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
