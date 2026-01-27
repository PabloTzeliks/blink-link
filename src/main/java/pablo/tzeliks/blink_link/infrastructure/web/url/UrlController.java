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

import java.net.URI;

/**
 * TODO : new Docs
 *
 * @author Pablo Tzeliks
 * @version 2.0.0
 * @since 1.0.0
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

    @GetMapping("/{shortCode}")
    public ResponseEntity<UrlResponse> access(@PathVariable String shortCode) {

        ResolveUrlRequest request = new ResolveUrlRequest(shortCode);

        UrlResponse response = resolveUrl.execute(request);
        return ResponseEntity.ok(response);
    }
}
