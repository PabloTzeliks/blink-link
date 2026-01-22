package pablo.tzeliks.blink_link.application.url.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pablo.tzeliks.blink_link.domain.url.ports.ShortenLogic;
import pablo.tzeliks.blink_link.domain.url.model.Url;
import pablo.tzeliks.blink_link.infraestructure.persistence.repository.PostgresUrlRepository;

/**
 * Service layer for URL shortening and resolution operations.
 * <p>
 * This service implements the core business logic for the URL shortener, including
 * the "Two-Step Save" persistence strategy and URL resolution. It coordinates between
 * the repository layer and the encoding logic to generate unique short codes.
 * <p>
 * <b>Key Architectural Decision - Two-Step Save Strategy:</b>
 * The service uses a two-phase persistence approach to generate short codes:
 * <ol>
 *   <li>Save the entity with only the original URL to obtain a database-generated ID</li>
 *   <li>Convert the numeric ID to Base62 encoding to create the short code</li>
 *   <li>Update the entity with the short code (within the same transaction)</li>
 * </ol>
 * This strategy ensures uniqueness by leveraging the database's sequential ID generation
 * while maintaining human-friendly short codes through Base62 encoding.
 * <p>
 * All persistence operations are transactional to maintain data consistency.
 *
 * @author Pablo Tzeliks
 * @since 1.0.0
 */
@Service
public class UrlService {

    private PostgresUrlRepository postgresUrlRepository;
    private ShortenLogic encoder;

    public UrlService(PostgresUrlRepository postgresUrlRepository, ShortenLogic encoder) {
        this.postgresUrlRepository = postgresUrlRepository;
        this.encoder = encoder;
    }

    /**
     * Shortens a long URL using the Two-Step Save algorithm.
     * <p>
     * This method implements the core URL shortening logic through a multi-step process:
     * <ol>
     *   <li><b>Validation:</b> Ensures the input URL is not null or empty</li>
     *   <li><b>First Save:</b> Persists a new UrlEntity with only the original URL,
     *       triggering the database to generate a unique sequential ID</li>
     *   <li><b>Encoding:</b> Converts the numeric ID to a Base62-encoded short code
     *       using the configured {@link ShortenLogic} implementation</li>
     *   <li><b>Update:</b> Sets the short code on the entity (automatically persisted
     *       due to JPA dirty checking within the transaction)</li>
     * </ol>
     * <p>
     * <b>Why Two-Step Save?</b> This approach leverages the database's ability to generate
     * unique IDs while allowing us to create human-friendly short codes derived from those IDs.
     * The entire operation is wrapped in a {@code @Transactional} boundary to ensure
     * atomicity and consistency.
     * <p>
     * <b>Null Safety:</b> The method includes input validation and throws an exception
     * if the URL is null or empty, preventing invalid data from entering the system.
     *
     * @param longUrl the original URL to be shortened; must not be null or empty
     * @return the persisted {@link Url} containing both the original URL and
     *         the generated short code
     * @throws IllegalArgumentException if the provided URL is null or empty
     */
    @Transactional
    public Url shorten(String longUrl) {

        if (longUrl == null || longUrl.isEmpty()) { throw new IllegalArgumentException("The URL cannot be empty."); }

        Url rawUrl = new Url(longUrl);

        Url savedUrl = postgresUrlRepository.save(rawUrl);

        String shortCode = encoder.encode(savedUrl.getId());
        savedUrl.setShortCode(shortCode);

        return savedUrl;
    }

    /**
     * Resolves a short code to its corresponding URL entity.
     * <p>
     * This method performs a lookup in the database to find the URL entity associated
     * with the provided short code. It demonstrates safe handling of {@link java.util.Optional}
     * by converting the result to {@code null} when no match is found.
     * <p>
     * <b>Null Safety:</b> The method uses {@code Optional.orElse(null)} to handle
     * the case where no URL entity exists for the given short code. This explicit
     * null handling allows the controller layer to easily distinguish between
     * found and not-found cases for proper HTTP status code responses.
     *
     * @param shortCode the short code to look up in the database
     * @return the {@link Url} associated with the short code, or {@code null}
     *         if no matching entity is found
     */
    public Url resolve(String shortCode) {

        return postgresUrlRepository.findByShortCode(shortCode).orElse(null);
    }
}
