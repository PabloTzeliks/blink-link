package pablo.tzeliks.blink_link.infrastructure.web.url;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import pablo.tzeliks.blink_link.application.url.dto.*;
import pablo.tzeliks.blink_link.application.url.usecase.CheckCodeAvailabilityUseCase;
import pablo.tzeliks.blink_link.application.url.usecase.GetUrlDetailsUseCase;
import pablo.tzeliks.blink_link.application.url.usecase.ShortenUrlUseCase;

import java.net.URI;

/**
 * @author Pablo Tzeliks
 * @version 4.0.0
 * @since 1.0.0
 */
@Validated
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

        CodeAvailabilityRequest request = new CodeAvailabilityRequest(code);

        return ResponseEntity.ok(checkCodeUseCase.execute(request));
    }
}
