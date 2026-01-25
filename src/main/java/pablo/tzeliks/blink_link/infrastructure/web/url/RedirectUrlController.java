package pablo.tzeliks.blink_link.infrastructure.web.url;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import pablo.tzeliks.blink_link.application.url.dto.ResolveUrlRequest;
import pablo.tzeliks.blink_link.application.url.usecase.ResolveUrlUseCase;

import java.net.URI;

@RestController
public class RedirectUrlController {

    private final ResolveUrlUseCase resolveUrl;

    public RedirectUrlController(ResolveUrlUseCase resolveUrl) {
        this.resolveUrl = resolveUrl;
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {

        var request = new ResolveUrlRequest(shortCode);
        var response = resolveUrl.execute(request);

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(response.originalUrl()))
                .build();
    }
}
