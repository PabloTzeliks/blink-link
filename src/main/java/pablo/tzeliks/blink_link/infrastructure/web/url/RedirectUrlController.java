package pablo.tzeliks.blink_link.infrastructure.web.url;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import pablo.tzeliks.blink_link.application.url.dto.ResolveUrlRequest;
import pablo.tzeliks.blink_link.application.url.usecase.RedirectUrlUseCase;

import java.net.URI;

/**
 * @author Pablo Tzeliks
 * @version 4.0.0
 * @since 1.0.0
 * @see RedirectUrlUseCase
 */
@RestController
public class RedirectUrlController {

    private final RedirectUrlUseCase resolveUrl;

    public RedirectUrlController(RedirectUrlUseCase resolveUrl) {
        this.resolveUrl = resolveUrl;
    }

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
