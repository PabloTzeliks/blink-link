package pablo.tzeliks.blink_link.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;

/**
 * TODO : new Docs
 *
 * @author Pablo Tzeliks
 * @version 2.0.0
 * @since 1.0.0
 */
@Entity
@Table(name = "url", schema = "public")
public class UrlEntity implements Persistable<Long> {

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

    @Column(name = "short_code", length = 7, nullable = false, unique = true)
    private String shortCode;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Transient
    private boolean isNew;

    public UrlEntity() { }

    public UrlEntity(Long id, String originalUrl, String shortCode, LocalDateTime createdAt) {
        this.id = id;
        this.originalUrl = originalUrl;
        this.shortCode = shortCode;
        this.createdAt = createdAt;
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

    public String getOriginalUrl() { return originalUrl; }

    public String getShortCode() { return shortCode; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}

