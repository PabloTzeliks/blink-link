package pablo.tzeliks.blink_link.application.url.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pablo.tzeliks.blink_link.application.url.dto.ResolveUrlRequest;
import pablo.tzeliks.blink_link.application.url.dto.UrlResponse;
import pablo.tzeliks.blink_link.application.url.mapper.UrlDtoMapper;
import pablo.tzeliks.blink_link.domain.url.exception.InvalidUrlException;
import pablo.tzeliks.blink_link.domain.url.exception.UrlNotFoundException;
import pablo.tzeliks.blink_link.domain.url.model.Url;
import pablo.tzeliks.blink_link.domain.url.ports.UrlRepositoryPort;

/**
 * Use case for resolving short codes back to their original URLs.
 * <p>
 * This service implements the business logic for looking up URLs by their
 * short codes. It performs validation on the short code and retrieves the
 * corresponding URL information from the repository.
 * <p>
 * <b>Business Rules:</b>
 * <ul>
 *   <li>Short codes must not be null or empty</li>
 *   <li>Short codes must exist in the database</li>
 * </ul>
 * <p>
 * <b>Error Handling:</b>
 * <ul>
 *   <li>Throws {@link InvalidUrlException} if the short code is null or empty</li>
 *   <li>Throws {@link UrlNotFoundException} if no URL exists for the provided short code</li>
 * </ul>
 * <p>
 * <b>Transaction Management:</b>
 * <p>
 * This method is annotated with {@code @Transactional(readOnly = true)}, optimizing
 * the database query for read-only access. This improves performance and signals
 * to the persistence layer that no write operations will occur.
 *
 * @author Pablo Tzeliks
 * @version 2.0.0
 * @since 1.0.0
 * @see UrlRepositoryPort
 */
@Service
public class ResolveUrlUseCase {

    private final UrlRepositoryPort repository;
    private final UrlDtoMapper mapper;

    public ResolveUrlUseCase(UrlRepositoryPort repository, UrlDtoMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    /**
     * Executes the URL resolution process.
     * <p>
     * This method performs the following operations:
     * <ol>
     *   <li>Validates the short code format (not null, not empty)</li>
     *   <li>Queries the repository for the URL associated with the short code</li>
     *   <li>Throws an exception if the URL is not found</li>
     *   <li>Converts the domain model to a DTO for the response</li>
     * </ol>
     * <p>
     * <b>Validations Applied:</b>
     * <ul>
     *   <li>Short code must not be null</li>
     *   <li>Short code must not be empty</li>
     * </ul>
     *
     * @param request the resolution request containing the short code to look up
     * @return a {@link UrlResponse} containing the original URL, short code, full short URL, and creation timestamp
     * @throws InvalidUrlException if the short code is null or empty
     * @throws UrlNotFoundException if no URL exists for the provided short code
     */
    @Transactional(readOnly = true)
    public UrlResponse execute(ResolveUrlRequest request) {

        String shortCode = request.shortCode();

        // Validates URL format
        if (shortCode == null || shortCode.isEmpty()) {
            throw new InvalidUrlException("Short Code cannot be null or empty");
        }

        Url urlDb = repository.findByShortCode(shortCode)
                .orElseThrow(() ->
                        new UrlNotFoundException("URL not found for the provided short code: " + shortCode));

        return mapper.toDto(urlDb);
    }
}
}