package pablo.tzeliks.blink_link.infrastructure.url.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pablo.tzeliks.blink_link.infrastructure.url.persistence.entity.UrlEntity;

import java.util.Optional;

/**
 * Spring Data JPA repository interface for URL entities.
 * <p>
 * This interface extends {@link JpaRepository} to provide CRUD operations and
 * custom query methods for {@link UrlEntity} objects. Spring Data JPA automatically
 * generates the implementation at runtime based on method naming conventions and
 * annotations.
 * <p>
 * <b>Spring Data JPA Benefits:</b>
 * <ul>
 *   <li><b>Automatic Implementation:</b> No need to write boilerplate repository code</li>
 *   <li><b>Query Derivation:</b> Methods like {@code findByShortCode} are automatically implemented</li>
 *   <li><b>Native Queries:</b> Supports custom SQL queries via {@code @Query} annotation</li>
 *   <li><b>Transaction Management:</b> Integrates with Spring's transaction management</li>
 * </ul>
 * <p>
 * <b>Why Two Repository Layers?</b>
 * <p>
 * This JPA repository is an infrastructure concern that works with {@link UrlEntity}
 * (persistence model). The {@code PostgresUrlRepository} adapts this to the domain's
 * {@code UrlRepositoryPort}, working with {@code Url} domain objects. This separation
 * maintains clean architecture principles.
 *
 * @author Pablo Tzeliks
 * @version 2.0.0
 * @since 1.0.0
 * @see UrlEntity
 * @see PostgresUrlRepository
 */
@Repository
public interface JpaUrlRepository extends JpaRepository<UrlEntity, Long> {

    /**
     * Finds a URL entity by its short code.
     * <p>
     * Spring Data JPA automatically implements this method based on the method name.
     * The query is derived as: {@code SELECT * FROM url WHERE short_code = ?}
     * <p>
     * The {@code short_code} field is indexed and unique, ensuring optimal query performance.
     *
     * @param shortCode the short code to search for
     * @return an {@link Optional} containing the entity if found, or empty otherwise
     */
    Optional<UrlEntity> findByShortCode(String shortCode);

    /**
     * Retrieves the next value from the PostgreSQL sequence.
     * <p>
     * This native SQL query directly calls PostgreSQL's {@code nextval()} function
     * to obtain the next ID from the {@code url_id_seq} sequence. This ID is used
     * to generate the Base62 short code before entity insertion.
     * <p>
     * <b>Why Native Query?</b> Spring Data JPA doesn't provide a built-in way to
     * call sequence functions, so we use a native query for direct database access.
     *
     * @return the next available ID from the sequence
     */
    @Query(value = "SELECT nextval('url_id_seq')", nativeQuery = true)
    Long nextId();
}

