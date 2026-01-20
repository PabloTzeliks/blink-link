package pablo.tzeliks.blink_link.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pablo.tzeliks.blink_link.dto.UrlRequest;
import pablo.tzeliks.blink_link.dto.UrlResponse;
import pablo.tzeliks.blink_link.model.UrlEntity;
import pablo.tzeliks.blink_link.service.UrlService;

import java.net.URI;

@RestController
public class UrlController {

    private UrlService urlService;

    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    @PostMapping("url/v1/shorten")
    public ResponseEntity<UrlResponse> encode(@RequestBody UrlRequest request, HttpServletRequest servletRequest) {

        UrlEntity url = urlService.shorten(request.url());

        // Construct a Domain Dinamic Redirect URL
        // http://localhost:8080/ + short code
        String redirectUrl = servletRequest.getRequestURL().toString().replace("url/v1/shorten", url.getShortCode());

        return ResponseEntity.ok(new UrlResponse(redirectUrl));
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> access(@PathVariable(name = "shortCode") String shortCode) {

        UrlEntity url = urlService.resolve(shortCode);

        if (url == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(url.getOriginalUrl()));

        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
}
