package pablo.tzeliks.blink_link.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pablo.tzeliks.blink_link.model.UrlEntity;
import pablo.tzeliks.blink_link.service.UrlService;

import java.net.URI;

@RestController
@RequestMapping("/url/v1")
public class UrlController {

    private UrlService urlService;

    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    @PostMapping("/shorten")
    public ResponseEntity<String> encode(@RequestBody ShortenRequest request, HttpServletRequest servletRequest) {

        UrlEntity url = urlService.shorten(originalUrl);

        return ResponseEntity.ok(url.getShortCode());
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<String> access(@PathVariable(name = "shortCode") String shortCode) {

        var url = urlService.resolve(shortCode);

        if (url == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Short URL not found");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(url.getOriginalUrl()));

        return ResponseEntity.status(HttpStatus.FOUND).body(url.getOriginalUrl());
    }
}
