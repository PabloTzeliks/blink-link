package pablo.tzeliks.blink_link.infrastructure.web.url;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import pablo.tzeliks.blink_link.application.url.dto.CodeAvailabilityResponse;
import pablo.tzeliks.blink_link.application.url.dto.CreateUrlRequest;
import pablo.tzeliks.blink_link.application.url.dto.ResolveUrlRequest;
import pablo.tzeliks.blink_link.application.url.dto.UrlDetailsResponse;
import pablo.tzeliks.blink_link.application.url.usecase.CheckCodeAvailabilityUseCase;
import pablo.tzeliks.blink_link.application.url.usecase.GetUrlDetailsUseCase;
import pablo.tzeliks.blink_link.application.url.usecase.ShortenUrlUseCase;

import java.net.URI;

/**
 * @author Pablo Tzeliks
 * @version 4.0.0
 * @since 1.0.0
 */
@RestController()
@RequestMapping("api/v3/urls")
public class UrlController {

    private final ShortenUrlUseCase shortenUrl;
    private final GetUrlDetailsUseCase urlDetailsUseCase;
    private final CheckCodeAvailabilityUseCase checkCodeUseCase;

    public UrlController(ShortenUrlUseCase shortenUrl,
                         GetUrlDetailsUseCase urlDetailsUseCase,
                         CheckCodeAvailabilityUseCase checkCodeUseCase) {

        this.shortenUrl = shortenUrl;
        this.urlDetailsUseCase = urlDetailsUseCase;
        this.checkCodeUseCase = checkCodeUseCase;
    }

    @PostMapping("/shorten")
    public ResponseEntity<UrlDetailsResponse> encode(@Valid @RequestBody CreateUrlRequest request, UriComponentsBuilder uriBuilder) {

        UrlDetailsResponse response = shortenUrl.execute(request);

        URI pathLocation = uriBuilder.path("/api/v2/urls/{shortCode}")
                .buildAndExpand(response.shortCode())
                .toUri();

        return ResponseEntity
                .created(pathLocation)
                .body(response);
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<UrlDetailsResponse> access(@PathVariable String shortCode) {

        ResolveUrlRequest request = new ResolveUrlRequest(shortCode);

        UrlDetailsResponse response = urlDetailsUseCase.execute(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/codes/{code}/availability")
    public ResponseEntity<CodeAvailabilityResponse> check(@PathVariable String code) {

        return ResponseEntity.ok(checkCodeUseCase.execute(code));
    }
}
