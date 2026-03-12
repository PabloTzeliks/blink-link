package pablo.tzeliks.blink_link.domain.url.ports;

import pablo.tzeliks.blink_link.domain.url.model.Url;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Port interface for URL persistence operations in the hexagonal architecture.
 * <p>
 * This interface defines the contract for storing and retrieving URL entities.
 * It represents an output port in the hexagonal (ports and adapters) architecture,
 * abstracting the persistence mechanism from the domain and application layers.
 * <p>
 * <b>Repository Pattern:</b>
 * <p>
 * This interface implements the Repository pattern, providing a collection-like
 * interface for accessing domain objects. It hides the details of data access
 * (SQL, NoSQL, in-memory, etc.) behind a domain-focused API.
 * <p>
 * <b>Benefits:</b>
 * <ul>
 *   <li><b>Abstraction:</b> Domain logic doesn't depend on database specifics</li>
 *   <li><b>Testability:</b> Easy to mock for unit tests without a database</li>
 *   <li><b>Flexibility:</b> Can swap implementations (PostgreSQL, MongoDB, Redis, etc.)</li>
 *   <li><b>Clean Architecture:</b> Maintains dependency direction (domain ← infrastructure)</li>
 * </ul>
 * <p>
 * <b>Implementation:</b>
 * <p>
 * The primary implementation is {@code PostgresUrlRepository}, which uses
 * Spring Data JPA and PostgreSQL for persistence. The implementation coordinates
 * between the JPA repository and the domain layer using entity-domain mapping.
 *
 * @author Pablo Tzeliks
 * @version 2.0.0
 * @since 1.0.0
 */
public interface UrlRepositoryPort {

    /**
     * Retrieves the next available ID from the database sequence.
     * <p>
     * This method obtains a unique sequential ID from the PostgreSQL sequence
     * ({@code url_id_seq}). The ID is used as input for the Base62 encoding
     * algorithm to generate the short code.
     * <p>
     * <b>Why Pre-generate IDs?</b>
     * <ul>
     *   <li>Enables deterministic short code generation before persistence</li>
     *   <li>Allows the short code to be part of the entity before insertion</li>
     *   <li>Supports the {@code Persistable} optimization for insert performance</li>
     * </ul>
     *
     * @return the next available ID from the sequence
     */
    Long nextId();

    /**
     * Persists a URL domain object to the database.
     * <p>
     * This method saves the URL mapping, storing the relationship between the
     * original URL and its short code. The operation is typically executed within
     * a transaction to ensure consistency.
     *
     * @param url the URL domain object to persist; must not be {@code null}
     * @return the persisted URL object, potentially with updated fields (e.g., creation timestamp)
     */
    Url save(Url url);

    /**
     * Retrieves a URL by its unique identifier.
     * <p>
     * This method looks up a URL using its numeric ID (primary key).
     *
     * @param id the unique identifier to search for; must not be {@code null}
     * @return an {@link Optional} containing the URL if found, or empty if not found
     */
    Optional<Url> findById(Long id);

    /**
     * Retrieves a URL by its short code.
     * <p>
     * This method looks up a URL using its Base62-encoded short code. This is
     * the primary lookup method used when resolving short URLs to their original
     * destinations.
     * <p>
     * <b>Performance:</b> The {@code short_code} field is indexed and unique,
     * ensuring fast lookups even with millions of URLs in the database.
     *
     * @param shortCode the short code to search for; must not be {@code null}
     * @return an {@link Optional} containing the URL if found, or empty if not found
     */
    Optional<Url> findByShortCode(String shortCode);

    int deleteExpiredInBatch(LocalDateTime referenceTime, int batchSize);
}
