package pablo.tzeliks.blink_link.domain.url.model;

import pablo.tzeliks.blink_link.domain.common.exception.DomainException;
import pablo.tzeliks.blink_link.domain.url.exception.InvalidUrlException;
import pablo.tzeliks.blink_link.domain.url.strategy.ExpirationCalculationStrategy;

import java.time.LocalDateTime;

/**
 * @author Pablo Tzeliks
 * @version 3.0.0
 * @since 2.0.0
 */
public class Url {

    private final Long id;
    private final String originalUrl;
    private final String shortCode;
    private final LocalDateTime createdAt;
    private final LocalDateTime expirationDate;

    private Url(Long id,
                String originalUrl,
                String shortCode,
                LocalDateTime createdAt,
                LocalDateTime expirationDate) {

        validateOriginalUrl(originalUrl);

        if (shortCode == null || shortCode.isBlank()) {
            throw new DomainException("Short code cannot be null or blank");
        }

        this.id = id;
        this.originalUrl = originalUrl;
        this.shortCode = shortCode;
        this.createdAt = createdAt;
        this.expirationDate = expirationDate;
    }

    private void validateOriginalUrl(String url) {

        if (url == null || url.isBlank()) {
            throw new InvalidUrlException("Original URL cannot be null or blank");
        }

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw new InvalidUrlException("Original URL must start with http:// or https://");
        }
    }

    public static Url create(Long id,
                             String originalUrl,
                             String shortCode,
                             ExpirationCalculationStrategy expirationStrategy) {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expirationDate = expirationStrategy.calculateExpirationDate(now);

        return new Url(id, originalUrl, shortCode, now, expirationDate);
    }

    public static Url restore(Long id,
                              String originalUrl,
                              String shortCode,
                              LocalDateTime createdAt,
                              LocalDateTime expirationDate) {

        return new Url(id, originalUrl, shortCode, createdAt, expirationDate);
    }

    public boolean isExpired() {

        if (this.expirationDate == null) {
            return false;
        }

        return LocalDateTime.now().isAfter(this.expirationDate);
    }

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

    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }
}
