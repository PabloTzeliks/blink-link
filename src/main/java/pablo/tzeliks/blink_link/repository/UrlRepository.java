package pablo.tzeliks.blink_link.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pablo.tzeliks.blink_link.model.UrlEntity;

import java.util.Optional;

/**
 * Repository interface for performing CRUD operations on {@link UrlEntity}.
 * <p>
 * This interface extends {@link JpaRepository} which provides out-of-the-box
 * implementations for standard database operations including save, findById,
 * findAll, delete, and more. By extending JpaRepository with type parameters
 * {@code <UrlEntity, Long>}, we declare that:
 * <ul>
 *   <li>{@link UrlEntity} is the domain class this repository manages</li>
 *   <li>{@link Long} is the type of the primary key (ID field) in UrlEntity</li>
 * </ul>
 * <p>
 * <b>Why JpaRepository?</b> Spring Data JPA automatically generates the implementation
 * of this interface at runtime, eliminating the need for boilerplate data access code.
 * This allows us to focus on business logic rather than SQL queries.
 * <p>
 * Custom query methods follow Spring Data JPA naming conventions, where method names
 * are parsed to generate appropriate queries automatically.
 *
 * @author Pablo Tzeliks
 * @since 1.0.0
 */
@Repository
public interface UrlRepository extends JpaRepository<UrlEntity, Long> {

    /**
     * Finds a URL entity by its short code.
     * <p>
     * This custom query method follows Spring Data JPA's method naming convention.
     * The method name {@code findByShortCode} is automatically parsed to generate
     * a query equivalent to: {@code SELECT * FROM url WHERE short_code = ?}
     * <p>
     * The return type {@link Optional} provides null-safe handling, allowing
     * callers to elegantly handle cases where no entity matches the short code.
     * <p>
     * <b>Index Consideration:</b> Since short codes are used frequently for lookups
     * and are marked as unique in the entity, database queries on this field should
     * be efficient. The unique constraint on short_code ensures query performance.
     *
     * @param shortCode the short code to search for; typically a Base62-encoded string
     * @return an {@link Optional} containing the matching {@link UrlEntity} if found,
     *         or an empty Optional if no entity exists with the given short code
     */
    Optional<UrlEntity> findByShortCode(String shortCode);
}
