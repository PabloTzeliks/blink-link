package pablo.tzeliks.blink_link.domain.url.model;

import pablo.tzeliks.blink_link.domain.common.exception.DomainException;
import pablo.tzeliks.blink_link.domain.url.exception.InvalidUrlException;
import pablo.tzeliks.blink_link.domain.url.strategy.ExpirationCalculationStrategy;
import pablo.tzeliks.blink_link.domain.user.model.Plan;

import java.time.LocalDateTime;

/**
 * Domain model representing a shortened URL in the business domain.
 * <p>
 * This class encapsulates the core URL shortening concept, representing the relationship
 * between an original long URL and its shortened counterpart. It serves as the central
 * domain entity in the URL shortener business logic, enforcing business rules and
 * validations at the domain level.
 * <p>
 * <b>Immutability:</b>
 * <p>
 * All fields are declared as {@code final}, making this class immutable. Once a {@code Url}
 * object is created, its state cannot be changed. This design promotes thread safety,
 * prevents unintended side effects, and aligns with functional programming principles.
 * <p>
 * <b>Validation Rules:</b>
 * <p>
 * The constructor performs strict validation to ensure data integrity:
 * <ul>
 *   <li>Original URL must not be {@code null} or blank</li>
 *   <li>Original URL must start with "http://" or "https://"</li>
 * </ul>
 * <p>
 * These validations are enforced at the domain level, ensuring that invalid URLs
 * can never exist in the system, regardless of which layer creates the object.
 * <p>
 * <b>Business Logic:</b>
 * <p>
 * This domain model represents the core business concept without any infrastructure
 * concerns. It is part of the hexagonal architecture's domain layer and is completely
 * independent of frameworks, databases, or external systems.
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
    private final LocalDateTime expirationDate;

    private Url(Long id, String originalUrl, String shortCode, LocalDateTime expirationDate) {
        validateOriginalUrl(originalUrl);

        if (shortCode == null || shortCode.isBlank()) {
            throw new DomainException("Short code cannot be null or blank");
        }

        this.id = id;
        this.originalUrl = originalUrl;
        this.shortCode = shortCode;
        this.createdAt = LocalDateTime.now();
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

    public static Url create(String originalUrl, String shortCode, ExpirationCalculationStrategy expirationStrategy) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expirationDate = expirationStrategy.calculateExpirationDate(now);

        return new Url(null, originalUrl, shortCode, now, expirationDate);
    }

    /**
     * FACTORY METHOD: Restaura uma URL existente a partir do banco de dados (Infra Layer).
     * Recebe todos os atributos exatos como estão armazenados.
     */
    public static Url restore(Long id, String originalUrl, String shortCode, LocalDateTime createdAt, LocalDateTime expirationDate) {
        return new Url(id, originalUrl, shortCode, createdAt, expirationDate);
    }

    /**
     * COMPORTAMENTO DE DOMÍNIO: A própria URL sabe dizer se está expirada.
     */
    public boolean isExpired() {
        if (this.expirationDate == null) {
            return false; // URLs com expirationDate null são vitalícias
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
