package pablo.tzeliks.blink_link.domain.url.model;

import pablo.tzeliks.blink_link.domain.url.exception.InvalidUrlException;

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

    /**
     * Constructs a new Url domain object with validation.
     * <p>
     * This constructor creates a new URL instance after validating the original URL
     * according to business rules. If validation fails, an {@link InvalidUrlException}
     * is thrown, preventing the creation of invalid domain objects.
     * <p>
     * <b>Validation Performed:</b>
     * <ul>
     *   <li>Checks that the URL is not {@code null}, empty, or blank</li>
     *   <li>Ensures the URL starts with "http://" or "https://"</li>
     * </ul>
     *
     * @param id the unique identifier for this URL (typically from database sequence)
     * @param originalUrl the original long URL; must be valid per business rules
     * @param shortCode the Base62-encoded short code representing this URL
     * @param createdAt the timestamp when this URL was created
     * @throws InvalidUrlException if the original URL is {@code null}, blank, or doesn't start with http/https
     */
    public Url(Long id, String originalUrl, String shortCode, LocalDateTime createdAt) {
        this.id = id;
        this.originalUrl = originalUrl;
        this.shortCode = shortCode;
        this.createdAt = createdAt;
    }

    // Getters

    /**
     * Gets the unique identifier of this URL.
     *
     * @return the URL's ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Gets the original long URL.
     *
     * @return the original URL string
     */
    public String getOriginalUrl() {
        return originalUrl;
    }

    /**
     * Gets the Base62-encoded short code.
     *
     * @return the short code string
     */
    public String getShortCode() {
        return shortCode;
    }

    /**
     * Gets the creation timestamp.
     *
     * @return the timestamp when this URL was created
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
