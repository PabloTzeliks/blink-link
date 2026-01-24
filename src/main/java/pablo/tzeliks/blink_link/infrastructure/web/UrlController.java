package pablo.tzeliks.blink_link.infrastructure.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pablo.tzeliks.blink_link.application.url.dto.CreateUrlRequest;
import pablo.tzeliks.blink_link.application.url.dto.UrlResponse;
import pablo.tzeliks.blink_link.application.url.usecase.ResolveUrlUseCase;
import pablo.tzeliks.blink_link.application.url.usecase.ShortenUrlUseCase;
import pablo.tzeliks.blink_link.domain.url.model.Url;

import java.net.URI;

/**
 * TODO : new Docs
 *
 * @author Pablo Tzeliks
 * @version 2.0.0
 * @since 1.0.0
 */
@RestController
public class UrlController {

    private final ShortenUrlUseCase shortenUrlUseCase;
    private final ResolveUrlUseCase resolveUrlUseCase;

    public UrlController(ShortenUrlUseCase shortenUrlUseCase, ResolveUrlUseCase resolveUrlUseCase) {
        this.shortenUrlUseCase = shortenUrlUseCase;
        this.resolveUrlUseCase = resolveUrlUseCase;
    }

    @PostMapping("url/v1/shorten")
    public ResponseEntity<UrlResponse> encode(@RequestBody CreateUrlRequest request, HttpServletRequest servletRequest) {

        Url url = urlService.shorten(request.url());

        // Construct a Domain Dynamic Redirect URL
        // http://localhost:8080/ + short code
        String redirectUrl = servletRequest.getRequestURL().toString().replace("url/v1/shorten", url.getShortCode());

        return ResponseEntity.ok(new UrlResponse(redirectUrl));
    }

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
