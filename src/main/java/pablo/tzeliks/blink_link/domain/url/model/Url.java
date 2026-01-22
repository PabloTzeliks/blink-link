package pablo.tzeliks.blink_link.domain.url.model;

import pablo.tzeliks.blink_link.domain.url.exception.InvalidUrlException;

import java.time.LocalDateTime;

/**
 * TODO : new Docs
 *
 * @author Pablo Tzeliks
 * @version 2.0.0
 * @since 2.0.0
 */
public class Url {

    private final Long id;
    private final String originalUrl;
    private final String shortCode;
    private final LocalDateTime createdAt;

    public Url(Long id, String originalUrl, String shortCode, LocalDateTime createdAt) {

        validateOriginalUrl(originalUrl);

        this.id = id;
        this.originalUrl = originalUrl;
        this.shortCode = shortCode;
        this.createdAt = createdAt;
    }

    private void validateOriginalUrl(String url) {
        if (url == null || url.isBlank()) throw new InvalidUrlException("Invalid URL");
        if (!url.startsWith("http")) throw new InvalidUrlException("URL must start with http/https");
    }

    // Getters

    public Long getId() {
        return id;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public String getShortCode() {
        return shortCode;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
