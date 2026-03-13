package pablo.tzeliks.blink_link.infrastructure.url.persistence.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pablo.tzeliks.blink_link.domain.url.model.Url;
import pablo.tzeliks.blink_link.domain.url.ports.UrlRepositoryPort;
import pablo.tzeliks.blink_link.infrastructure.url.persistence.mapper.UrlEntityMapper;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * PostgreSQL implementation of the URL repository port.
 * <p>
 * This class acts as an adapter in the hexagonal architecture, implementing the
 * domain's {@link UrlRepositoryPort} interface while coordinating with the infrastructure's
 * {@link JpaUrlRepository}. It bridges the gap between the domain layer (which works
 * with {@code Url} objects) and the infrastructure layer (which works with {@code UrlEntity} objects).
 * <p>
 * <b>Adapter Pattern:</b>
 * <p>
 * This repository implements the Adapter pattern, translating between two different
 * interfaces and data models:
 * <ul>
 *   <li><b>Domain Side:</b> Works with {@code Url} domain objects and {@code UrlRepositoryPort} interface</li>
 *   <li><b>Infrastructure Side:</b> Works with {@code UrlEntity} JPA entities and {@code JpaUrlRepository}</li>
 * </ul>
 * <p>
 * <b>Responsibilities:</b>
 * <ol>
 *   <li>Delegate persistence operations to the JPA repository</li>
 *   <li>Convert between domain objects ({@code Url}) and persistence entities ({@code UrlEntity})</li>
 *   <li>Maintain the dependency direction (domain ← infrastructure)</li>
 * </ol>
 * <p>
 * <b>Why This Design?</b>
 * <p>
 * Separating domain models from persistence models provides:
 * <ul>
 *   <li>Domain models free from JPA annotations and persistence concerns</li>
 *   <li>Ability to change persistence technology without affecting domain logic</li>
 *   <li>Clean separation between business logic and infrastructure</li>
 *   <li>Easier testing of domain logic without database dependencies</li>
 * </ul>
 *
 * @author Pablo Tzeliks
 * @version 2.0.0
 * @since 2.0.0
 * @see UrlRepositoryPort
 * @see JpaUrlRepository
 * @see UrlEntityMapper
 */
@Repository
public class PostgresUrlRepositoryAdapter implements UrlRepositoryPort {

    private final JpaUrlRepository repository;
    private final UrlEntityMapper mapper;
    private final EntityManager entityManager;

    public PostgresUrlRepositoryAdapter(JpaUrlRepository repository,
                                        UrlEntityMapper mapper,
                                        EntityManager entityManager) {
        this.repository = repository;
        this.mapper = mapper;
        this.entityManager = entityManager;
    }

    @Override
    public Long nextId() {

        return repository.nextId();
    }

    /**
     * Saves a URL domain object to the PostgreSQL database.
     * <p>
     * This method:
     * <ol>
     *   <li>Converts the domain object to a JPA entity using the mapper</li>
     *   <li>Persists the entity via the JPA repository</li>
     *   <li>Converts the persisted entity back to a domain object</li>
     * </ol>
     * <p>
     * The returned domain object may have updated fields (e.g., the creation
     * timestamp set by Hibernate's {@code @CreationTimestamp}).
     *
     * @param url the URL domain object to save
     * @return the saved URL domain object with any auto-generated fields populated
     */
    @Override
    public Url save(Url url) {

        return mapper.toDomain(repository.save(mapper.toEntity(url)));
    }

    /**
     * Finds a URL by its unique identifier.
     * <p>
     * This method delegates to the JPA repository and converts the result
     * from an entity to a domain object if found.
     *
     * @param id the ID to search for
     * @return an {@link Optional} containing the domain object if found, empty otherwise
     */
    @Override
    public Optional<Url> findById(Long id) {

        return repository.findById(id)
                .map(mapper::toDomain);
    }

    /**
     * Finds a URL by its short code.
     * <p>
     * This method delegates to the JPA repository and converts the result
     * from an entity to a domain object if found. This is the primary lookup
     * method used for URL resolution and redirection.
     *
     * @param shortCode the short code to search for
     * @return an {@link Optional} containing the domain object if found, empty otherwise
     */
    @Override
    public Optional<Url> findByShortCode(String shortCode) {

        return repository.findByShortCode(shortCode)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional
    public int deleteExpiredInBatch(LocalDateTime referenceTime, int batchSize) {

        String sql = """
            DELETE FROM urls
            WHERE id IN (
                SELECT id FROM urls
                WHERE expiration_date < :refTime
                LIMIT :batchSize
                FOR UPDATE SKIP LOCKED
            )
        """;

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("refTime", referenceTime);
        query.setParameter("batchSize", batchSize);

        return query.executeUpdate();
    }


}
