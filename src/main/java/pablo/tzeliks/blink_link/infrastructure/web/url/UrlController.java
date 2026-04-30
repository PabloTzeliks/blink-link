package pablo.tzeliks.blink_link.infrastructure.web.url;

import jakarta.validation.Valid;
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
    public ResponseEntity<UrlDetailsResponse> encode(@Valid @RequestBody CreateShortCodeRequest request, UriComponentsBuilder uriBuilder) {

        UrlDetailsResponse response = request.customCode() != null
                ? createCustomCode.execute(request)
                : shortenUrl.execute(request);

        URI location = uriBuilder
                .path("/api/v3/urls/{shortCode}")
                .buildAndExpand(response.shortCode())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<UrlDetailsResponse> access(@PathVariable String shortCode) {

        ResolveShortCodeRequest request = new ResolveShortCodeRequest(shortCode);

        UrlDetailsResponse response = urlDetailsUseCase.execute(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/codes/{code}/availability")
    public ResponseEntity<CodeAvailabilityResponse> check(@PathVariable String code) {

        CodeAvailabilityRequest request = new CodeAvailabilityRequest(code);

        return ResponseEntity.ok(checkCodeUseCase.execute(request));
    }
}
