package pablo.tzeliks.blink_link.infrastructure.web.url;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import pablo.tzeliks.blink_link.application.url.dto.ResolveUrlRequest;
import pablo.tzeliks.blink_link.application.url.usecase.ResolveUrlUseCase;

import java.net.URI;

/**
 * REST controller for URL redirection operations.
 * <p>
 * This controller handles the primary user-facing functionality of the URL shortener:
 * redirecting users from short codes to their original long URLs. It provides a simple,
 * root-level endpoint that accepts short codes and returns HTTP 302 (Found) redirects.
 * <p>
 * <b>Endpoint:</b>
 * <ul>
 *   <li>{@code GET /{shortUrl}} - Redirects to the original URL associated with the short code</li>
 * </ul>
 * <p>
 * This controller delegates URL resolution to {@link ResolveUrlUseCase} and returns
 * a standard HTTP redirect response with the Location header set to the original URL.
 * <p>
 * <b>Example Usage:</b>
 * <pre>
 * Request:  GET /abc123
 * Response: 302 Found
 *           Location: https://example.com/very/long/url
 * </pre>
 *
 * @author Pablo Tzeliks
 * @version 2.0.0
 * @since 1.0.0
 * @see ResolveUrlUseCase
 */
@RestController
public class RedirectUrlController {

    private final ResolveUrlUseCase resolveUrl;

    public RedirectUrlController(ResolveUrlUseCase resolveUrl) {
        this.resolveUrl = resolveUrl;
    }

    /**
     * Redirects a short URL to its original long URL.
     * <p>
     * This endpoint performs a lookup for the provided short code and, if found,
     * returns an HTTP 302 (Found) redirect response with the Location header set
     * to the original URL. This is the primary entry point for end users clicking
     * on shortened links.
     * <p>
     * <b>HTTP Response Codes:</b>
     * <ul>
     *   <li>{@code 302 Found} - Short code exists, redirect to original URL via Location header</li>
     *   <li>{@code 400 Bad Request} - Short code is null or empty</li>
     *   <li>{@code 404 Not Found} - No URL found for the provided short code</li>
     * </ul>
     * <p>
     * <b>Browser Behavior:</b> Most browsers and HTTP clients will automatically
     * follow the 302 redirect, transparently taking the user to the original URL.
     * The short URL will not be visible in the address bar after the redirect.
     *
     * @param shortUrl the short code extracted from the URL path
     * @return a {@link ResponseEntity} with status 302 and the Location header set to the original URL;
     *         the body is empty as per HTTP specification for redirect responses
     * @throws pablo.tzeliks.blink_link.domain.url.exception.UrlNotFoundException if no URL exists for the short code
     * @throws pablo.tzeliks.blink_link.domain.url.exception.InvalidUrlException if the short code is invalid
     */
    @GetMapping("/{shortUrl}")
    public ResponseEntity<Void> redirect(@PathVariable String shortUrl) {

        var request = new ResolveUrlRequest(shortUrl);
        var response = resolveUrl.execute(request);

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(response.originalUrl()))
                .build();
    }
}
