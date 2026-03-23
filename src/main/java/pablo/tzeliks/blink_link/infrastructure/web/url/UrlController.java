package pablo.tzeliks.blink_link.infrastructure.web.url;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import pablo.tzeliks.blink_link.application.url.dto.CreateUrlRequest;
import pablo.tzeliks.blink_link.application.url.dto.ResolveUrlRequest;
import pablo.tzeliks.blink_link.application.url.dto.UrlResponse;
import pablo.tzeliks.blink_link.application.url.usecase.ResolveUrlUseCase;
import pablo.tzeliks.blink_link.application.url.usecase.ShortenUrlUseCase;
import pablo.tzeliks.blink_link.infrastructure.web.dto.ErrorResponse;

import java.net.URI;

@RestController
@RequestMapping("api/v3/urls")
@Tag(name = "URL Management", description = "Endpoints for creating and retrieving shortened URLs")
public class UrlController {

    private final ShortenUrlUseCase shortenUrl;
    private final ResolveUrlUseCase resolveUrl;

    public UrlController(ShortenUrlUseCase shortenUrl, ResolveUrlUseCase resolveUrl) {
        this.shortenUrl = shortenUrl;
        this.resolveUrl = resolveUrl;
    }

    @Operation(summary = "Create a shortened URL", description = "Takes an original URL and generates a new shortened link.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "URL successfully shortened",
                    headers = @Header(name = HttpHeaders.LOCATION, description = "URI of the newly created resource", schema = @Schema(type = "string")),
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UrlResponse.class))),
            @ApiResponse(responseCode = "422", description = "Validation Failed - e.g., invalid URL format",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflict - e.g., custom alias already in use",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/shorten")
    public ResponseEntity<UrlResponse> encode(@Valid @RequestBody CreateUrlRequest request, UriComponentsBuilder uriBuilder) {

        UrlResponse response = shortenUrl.execute(request);

        URI pathLocation = uriBuilder.path("/api/v3/urls/{shortCode}")
                .buildAndExpand(response.shortCode())
                .toUri();

        return ResponseEntity
                .created(pathLocation)
                .body(response);
    }

    @Operation(summary = "Get URL details", description = "Retrieves the details of a shortened URL without performing a redirect.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "URL details retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UrlResponse.class))),
            @ApiResponse(responseCode = "404", description = "Url Not Found",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "410", description = "Url Expired",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{shortCode}")
    public ResponseEntity<UrlResponse> access(
            @Parameter(description = "The generated short code", required = true, example = "AbC123Xy") @PathVariable String shortCode) {

        ResolveUrlRequest request = new ResolveUrlRequest(shortCode);
        UrlResponse response = resolveUrl.execute(request);

        return ResponseEntity.ok(response);
    }
}
