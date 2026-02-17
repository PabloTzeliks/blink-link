package pablo.tzeliks.blink_link.infrastructure.web.url;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import pablo.tzeliks.blink_link.application.url.dto.CreateUrlRequest;
import pablo.tzeliks.blink_link.application.url.dto.ResolveUrlRequest;
import pablo.tzeliks.blink_link.application.url.dto.UrlResponse;
import pablo.tzeliks.blink_link.application.url.usecase.ResolveUrlUseCase;
import pablo.tzeliks.blink_link.application.url.usecase.ShortenUrlUseCase;
import pablo.tzeliks.blink_link.domain.url.exception.InvalidUrlException;
import pablo.tzeliks.blink_link.domain.url.exception.UrlNotFoundException;

import java.net.URI;

/**
 * REST controller for URL shortening operations.
 * <p>
 * This controller provides the main API endpoints for the URL shortener service,
 * allowing clients to create shortened URLs and retrieve information about existing ones.
 * All endpoints are versioned under {@code /api/v2/urls}.
 * <p>
 * <b>Endpoints:</b>
 * <ul>
 *   <li>{@code POST /api/v2/urls/shorten} - Create a new shortened URL</li>
 *   <li>{@code GET /api/v2/urls/{shortCode}} - Retrieve URL information by short code</li>
 * </ul>
 * <p>
 * This controller delegates business logic to the application layer use cases:
 * {@link ShortenUrlUseCase} and {@link ResolveUrlUseCase}.
 *
 * @author Pablo Tzeliks
 * @version 2.0.0
 * @since 1.0.0
 * @see ShortenUrlUseCase
 * @see ResolveUrlUseCase
 */
@RestController()
@RequestMapping("api/v2/urls")
public class UrlController {

    private final ShortenUrlUseCase shortenUrl;
    private final ResolveUrlUseCase resolveUrl;

    public UrlController(ShortenUrlUseCase shortenUrl, ResolveUrlUseCase resolveUrl) {
        this.shortenUrl = shortenUrl;
        this.resolveUrl = resolveUrl;
    }

    /**
     * Creates a shortened URL from a long URL.
     * <
     * This endpoint accepts a long URL and generates a unique short code for it.
     * The short code is created by:
     * <ol>
     *   <li>Generating a unique ID from a PostgreSQL sequence</li>
     *   <li>Encoding the ID using Base62 algorithm</li>
     *   <li>Storing the mapping in the database</li>
     * </ol>
     * <p>
     * <b>Request Validation:</b>
     * <ul>
     *   <li>The {@code originalUrl} field must not be blank (enforced by {@code @NotBlank})</li>
     *   <li>The {@code originalUrl} must be a valid URL format (enforced by {@code @URL})</li>
     *   <li>The URL must start with http/https (enforced by domain validation)</li>
     * </ul>
     * <p>
     * <b>HTTP Response Codes:</b>
     * <ul>
     *   <li>{@code 201 Created} - URL successfully shortened, Location header contains the short URL resource</li>
     *   <li>{@code 400 Bad Request} - Invalid URL format or encoding error</li>
     *   <li>{@code 422 Unprocessable Content} - Validation failed (e.g., blank URL, invalid format)</li>
     * </ul>
     *
     * @param request the URL creation request containing the original URL; must be valid per {@code @Valid} annotation
     * @param uriBuilder Spring-provided builder for constructing URIs in responses
     * @return a {@link ResponseEntity} with status 201 and the created {@link UrlResponse} in the body;
     *         the Location header contains the URI of the newly created short URL resource
     */
    @PostMapping("/shorten")
    public ResponseEntity<UrlResponse> encode(@Valid @RequestBody CreateUrlRequest request, UriComponentsBuilder uriBuilder) {

        UrlResponse response = shortenUrl.execute(request);

        URI pathLocation = uriBuilder.path("/api/v2/urls/{shortCode}")
                .buildAndExpand(response.shortCode())
                .toUri();

        return ResponseEntity
                .created(pathLocation)
                .body(response);
    }

    /**
     * Retrieves URL information by its short code.
     * <p>
     * This endpoint looks up a URL by its short code and returns the complete
     * URL information including the original URL, short code, full short URL,
     * and creation timestamp. This is an informational endpoint; for actual
     * redirection, use {@link RedirectUrlController}.
     * <p>
     * <b>HTTP Response Codes:</b>
     * <ul>
     *   <li>{@code 200 OK} - Short code found, returns {@link UrlResponse} with URL details</li>
     *   <li>{@code 400 Bad Request} - Short code is null or empty</li>
     *   <li>{@code 404 Not Found} - No URL found for the provided short code</li>
     * </ul>
     *
     * @param shortCode the short code to look up; captured from the URL path
     * @return a {@link ResponseEntity} with status 200 and the {@link UrlResponse} containing URL details
     * @throws UrlNotFoundException if no URL exists for the short code
     * @throws InvalidUrlException if the short code is invalid
     */
    @GetMapping("/{shortCode}")
    public ResponseEntity<UrlResponse> access(@PathVariable String shortCode) {

        ResolveUrlRequest request = new ResolveUrlRequest(shortCode);

        UrlResponse response = resolveUrl.execute(request);
        return ResponseEntity.ok(response);
    }
}
