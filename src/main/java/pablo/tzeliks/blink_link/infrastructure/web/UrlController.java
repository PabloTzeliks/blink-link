package pablo.tzeliks.blink_link.infrastructure.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pablo.tzeliks.blink_link.application.url.dto.CreateUrlRequest;
import pablo.tzeliks.blink_link.application.url.dto.UrlResponse;
import pablo.tzeliks.blink_link.domain.url.model.Url;

import java.net.URI;

/**
 * REST controller for URL shortening and redirection operations.
 * <p>
 * This controller implements a segregated design pattern with two distinct responsibilities:
 * <ul>
 *   <li>URL shortening API endpoint at {@code /url/v1/shorten} (POST) - Creates shortened URLs</li>
 *   <li>Redirection endpoint at {@code /{shortCode}} (GET) - Resolves and redirects to original URLs</li>
 * </ul>
 * <p>
 * The segregation ensures clean separation between API operations and user-facing redirects,
 * allowing different paths and HTTP methods to handle distinct concerns.
 * <p>
 * <b>Design Decision:</b> The API endpoint uses versioned path ({@code /url/v1/}) while the
 * redirection endpoint uses the root path ({@code /}) to provide the shortest possible URLs for end users.
 *
 * @author Pablo Tzeliks
 * @since 1.0.0
 */
@RestController
public class UrlController {

    private UrlService urlService;

    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    /**
     * Creates a shortened URL from the provided original URL.
     * <p>
     * This endpoint accepts a long URL and returns a shortened version that can be used
     * for redirection. The shortened URL is dynamically constructed based on the current
     * request URL to ensure portability across different deployment environments.
     * <p>
     * <b>URL Construction Logic:</b> The response URL is calculated by replacing the API
     * path segment {@code "url/v1/shorten"} with the generated short code in the request URL.
     * For example:
     * <pre>
     * Request URL: http://localhost:8080/url/v1/shorten
     * Short Code: abc123
     * Result: http://localhost:8080/abc123
     * </pre>
     * This approach ensures that the shortened URL uses the same domain and protocol
     * as the incoming request, supporting multiple environments without configuration changes.
     *
     * @param request the {@link CreateUrlRequest} containing the original URL to be shortened
     * @param servletRequest the HTTP servlet request used to extract the base URL for constructing
     *                       the dynamic redirect URL
     * @return a {@link ResponseEntity} containing the {@link UrlResponse} with the complete shortened URL
     */
    @PostMapping("url/v1/shorten")
    public ResponseEntity<UrlResponse> encode(@RequestBody CreateUrlRequest request, HttpServletRequest servletRequest) {

        Url url = urlService.shorten(request.url());

        // Construct a Domain Dynamic Redirect URL
        // http://localhost:8080/ + short code
        String redirectUrl = servletRequest.getRequestURL().toString().replace("url/v1/shorten", url.getShortCode());

        return ResponseEntity.ok(new UrlResponse(redirectUrl));
    }

    /**
     * Resolves a short code and redirects the client to the original URL.
     * <p>
     * This endpoint handles the redirection logic by looking up the short code in the database
     * and performing an HTTP 302 (Found) redirect to the original URL. If the short code is not
     * found, it returns a 404 (Not Found) status.
     * <p>
     * <b>HTTP Status Codes:</b>
     * <ul>
     *   <li>302 Found - Short code exists, redirecting to original URL</li>
     *   <li>404 Not Found - Short code does not exist in the database</li>
     * </ul>
     * <p>
     * The redirection is performed using the {@code Location} header in the HTTP response,
     * which instructs the browser to navigate to the original URL.
     *
     * @param shortCode the unique short code extracted from the URL path
     * @return a {@link ResponseEntity} with either a redirect response (302) containing the
     *         Location header, or a not found response (404) if the short code doesn't exist
     */
    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> access(@PathVariable(name = "shortCode") String shortCode) {

        Url url = urlService.resolve(shortCode);

        if (url == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(url.getOriginalUrl()));

        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
}
