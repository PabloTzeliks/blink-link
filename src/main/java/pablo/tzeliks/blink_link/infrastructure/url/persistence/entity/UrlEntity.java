package pablo.tzeliks.blink_link.infrastructure.url.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;

/**
 *
 * @author Pablo Tzeliks
 * @version 3.0.0
 * @since 1.0.0
 */
@Entity
@Table(name = "urls", schema = "public")
public class UrlEntity implements Persistable<Long> {

    /**
     * The unique identifier for this URL entity.
     * <p>
     * This ID is manually assigned using a PostgreSQL sequence ({@code url_id_seq})
     * before entity creation. The ID is used as the basis for generating the Base62
     * short code, ensuring uniqueness and allowing for efficient lookups.
     */
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * The original long URL that was submitted for shortening.
     * <p>
     * This field is required and cannot be null. It stores the full URL that
     * users will be redirected to when accessing the short code.
     */
    @Column(name = "original_url", nullable = false)
    private String originalUrl;

    /**
     * The Base62-encoded short code that represents this URL.
     * <p>
     * This is the unique, shortened identifier generated from the entity's ID
     * using Base62 encoding. It has a maximum length of 7 characters and must
     * be unique across all URLs in the system. This code is used in shortened
     * URLs to redirect users to the original URL.
     */
    @Column(name = "short_code", length = 7, nullable = false, unique = true)
    private String shortCode;

    /**
     * The timestamp when this URL was created.
     * <p>
     * This field is automatically populated by Hibernate using {@link CreationTimestamp}
     * when the entity is first persisted to the database. Any value set during construction
     * will be overwritten. The field is marked as non-updatable to prevent accidental modification.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expiration_date", nullable = false, updatable = false)
    private LocalDateTime expirationDate;

    /**
     * Transient flag indicating whether this entity is new (not yet persisted).
     * <p>
     * This field is not stored in the database ({@code @Transient}) and is used
     * to implement the {@link Persistable} interface. It is set to {@code true}
     * when constructing a new entity and automatically set to {@code false} after
     * the entity is persisted or loaded from the database.
     */
    @Transient
    private boolean isNew;

    /**
     * Protected no-argument constructor for JPA.
     * <p>
     * This constructor is required by JPA for entity instantiation during
     * database queries. It should not be used directly in application code.
     */
    protected UrlEntity() { }

    /**
     * Constructs a new UrlEntity with the specified values.
     * <p>
     * This constructor is used when creating new URL entities before persistence.
     * The {@code isNew} flag is automatically set to {@code true} to indicate this
     * is a new entity that hasn't been persisted yet.
     * <p>
     * Note: The {@code createdAt} parameter will be overwritten by Hibernate's
     * {@link CreationTimestamp} mechanism during insertion.
     *
     * @param id the unique identifier (pre-generated from sequence)
     * @param originalUrl the original long URL to be shortened
     * @param shortCode the Base62-encoded short code
     * @param createdAt the creation timestamp (will be overwritten by Hibernate)
     */
    public UrlEntity(Long id, String originalUrl, String shortCode, LocalDateTime createdAt, LocalDateTime expirationDate) {
        this.id = id;
        this.originalUrl = originalUrl;
        this.shortCode = shortCode;
        this.createdAt = createdAt;
        this.expirationDate = expirationDate;
    }
    // Persistable implementation

    /**
     * Returns the unique identifier of this entity.
     * <p>
     * Part of the {@link Persistable} interface implementation.
     *
     * @return the entity's ID, or {@code null} if not yet assigned
     */
    @Override
    public Long getId() {
        return id;
    }

    /**
     * Indicates whether this entity is new (not yet persisted).
     * <p>
     * Part of the {@link Persistable} interface implementation. This method
     * returns {@code true} for newly constructed entities and {@code false}
     * for entities that have been persisted or loaded from the database.
     * <p>
     * This optimization prevents Spring Data JPA from executing a SELECT query
     * before every INSERT operation.
     *
     * @return {@code true} if this entity is new, {@code false} otherwise
     */
    @Override
    public boolean isNew() {
        return isNew;
    }

    /**
     * Lifecycle callback that marks this entity as not new after persistence or loading.
     * <p>
     * This method is automatically invoked by JPA after the entity is persisted
     * ({@code @PostPersist}) or loaded from the database ({@code @PostLoad}).
     * It updates the {@code isNew} flag to prevent the entity from being treated
     * as a new entity in subsequent operations.
     */
    @PostLoad
    @PostPersist
    void markNotNew() {
        this.isNew = false;
    }

    // Getters

    /**
     * Gets the original long URL.
     *
     * @return the original URL string
     */
    public String getOriginalUrl() { return originalUrl; }

    /**
     * Gets the Base62-encoded short code.
     *
     * @return the short code string
     */
    public String getShortCode() { return shortCode; }

    /**
     * Gets the creation timestamp.
     *
     * @return the timestamp when this URL was created
     */
    public LocalDateTime getCreatedAt() { return createdAt; }

    public LocalDateTime getExpirationDate() { return expirationDate; }

    // Entity equality methods

    /**
     * Compares this entity with another object for equality.
     * <p>
     * Two {@code UrlEntity} instances are considered equal if they have the same
     * non-null ID. This follows the JPA best practice of using business keys for equality.
     *
     * @param o the object to compare with
     * @return {@code true} if the objects are equal, {@code false} otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UrlEntity urlEntity = (UrlEntity) o;
        return id != null && id.equals(urlEntity.id);
    }

    /**
     * Returns a hash code for this entity.
     * <p>
     * Uses the class hash code to ensure consistency across entity lifecycle
     * (new, managed, detached). This approach is recommended for JPA entities
     * to maintain hash code stability when entities are added to collections.
     *
     * @return a hash code value for this entity
     */
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
