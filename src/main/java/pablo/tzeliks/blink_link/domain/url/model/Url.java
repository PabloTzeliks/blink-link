package pablo.tzeliks.blink_link.domain.url.model;

import pablo.tzeliks.blink_link.domain.common.exception.DomainException;
import pablo.tzeliks.blink_link.domain.url.exception.InvalidUrlException;
import pablo.tzeliks.blink_link.domain.url.strategy.ExpirationCalculationStrategy;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author Pablo Tzeliks
 * @version 4.0.0
 * @since 2.0.0
 */
public class Url {

    private final Long id;
    private final UUID userId;
    private final String originalUrl;
    private final String shortCode;
    private final LocalDateTime createdAt;
    private final LocalDateTime expirationDate;

    private Url(Long id,
                UUID userId,
                String originalUrl,
                String shortCode,
                LocalDateTime createdAt,
                LocalDateTime expirationDate) {

        validateOriginalUrl(originalUrl);

        if (shortCode == null || shortCode.isBlank()) {
            throw new DomainException("Short code cannot be null or blank");
        }

        this.id = id;
        this.userId = userId;
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
                             UUID userId,
                             String originalUrl,
                             String shortCode,
                             ExpirationCalculationStrategy expirationStrategy) {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expirationDate = expirationStrategy.calculateExpirationDate(now);

        return new Url(id, userId, originalUrl, shortCode, now, expirationDate);
    }

    public static Url restore(Long id,
                              UUID userId,
                              String originalUrl,
                              String shortCode,
                              LocalDateTime createdAt,
                              LocalDateTime expirationDate) {

        return new Url(id, userId, originalUrl, shortCode, createdAt, expirationDate);
    }

    public boolean isExpired() {

        if (this.expirationDate == null) {
            return false;
        }

        return LocalDateTime.now().isAfter(this.expirationDate);
    }

    public long getSecondsUntilExpiry() {
        return Duration.between(LocalDateTime.now(), this.expirationDate).getSeconds();
    }

    public Long getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
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
