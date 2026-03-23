package pablo.tzeliks.blink_link.infrastructure.web.url;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import pablo.tzeliks.blink_link.application.url.dto.ResolveUrlRequest;
import pablo.tzeliks.blink_link.application.url.usecase.ResolveUrlUseCase;
import pablo.tzeliks.blink_link.infrastructure.web.dto.ErrorResponse;

import java.net.URI;

@RestController
@Tag(name = "Redirection", description = "Endpoint for redirecting short URLs to their original destinations")
public class RedirectUrlController {

    private final ResolveUrlUseCase resolveUrl;

    public RedirectUrlController(ResolveUrlUseCase resolveUrl) {
        this.resolveUrl = resolveUrl;
    }

    @Operation(summary = "Redirect short URL", description = "Resolves the short code and redirects the client to the original URL.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Successfully found the URL. Redirecting...",
                    headers = @Header(name = HttpHeaders.LOCATION, description = "The original URL to redirect to", schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "404", description = "Url Not Found - The short code does not exist",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "410", description = "Url Expired - The short code has passed its expiration date",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{shortUrl}")
    public ResponseEntity<Void> redirect(
            @Parameter(description = "The short code identifier", required = true, example = "AbC123Xy") @PathVariable String shortUrl) {

        var request = new ResolveUrlRequest(shortUrl);
        var response = resolveUrl.execute(request);

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(response.originalUrl()))
                .build();
    }
}
