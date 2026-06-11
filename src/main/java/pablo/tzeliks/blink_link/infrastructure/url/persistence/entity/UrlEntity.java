package pablo.tzeliks.blink_link.infrastructure.url.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 *
 * @author Pablo Tzeliks
 * @version 3.0.0
 * @since 1.0.0
 */
@Entity
@Table(name = "urls", schema = "public")
public class UrlEntity implements Persistable<Long> {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "original_url", nullable = false)
    private String originalUrl;

    @Column(name = "short_code", length = 20, nullable = false, unique = true)
    private String shortCode;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expiration_date", nullable = false, updatable = false)
    private LocalDateTime expirationDate;

    @Transient
    private boolean isNew;

    protected UrlEntity() { }

    public UrlEntity(Long id, UUID userId, String originalUrl, String shortCode, LocalDateTime createdAt, LocalDateTime expirationDate) {
        this.id = id;
        this.userId = userId;
        this.originalUrl = originalUrl;
        this.shortCode = shortCode;
        this.createdAt = createdAt;
        this.expirationDate = expirationDate;
        this.isNew = true;
    }
    // Persistable implementation

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PostLoad
    @PostPersist
    void markNotNew() {
        this.isNew = false;
    }

    // Getters

    public UUID getUserId() { return userId; }

    public String getOriginalUrl() { return originalUrl; }

    public String getShortCode() { return shortCode; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public LocalDateTime getExpirationDate() { return expirationDate; }

    // Entity equality methods

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UrlEntity urlEntity = (UrlEntity) o;
        return id != null && id.equals(urlEntity.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
