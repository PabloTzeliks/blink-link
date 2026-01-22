package pablo.tzeliks.blink_link.infraestructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * TODO : new Docs
 *
 * @author Pablo Tzeliks
 * @version 2.0.0
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "url", schema = "public")
public class UrlEntity {

    /**
     * Primary key identifier for the URL entity.
     * <p>
     * This field uses the {@code IDENTITY} generation strategy, which delegates
     * ID generation to the database (PostgreSQL SERIAL type). The generated ID
     * is the foundation for creating the short code via Base62 encoding.
     * <p>
     * <b>Important:</b> This ID is populated by the database only after the
     * entity is first saved, which is why the Two-Step Save pattern is necessary.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
     * The Base62-encoded short code derived from the entity's ID.
     * <p>
     * This field is:
     * <ul>
     *   <li><b>Derived:</b> Computed from the ID, not randomly generated</li>
     *   <li><b>Unique:</b> Database constraint ensures no duplicates</li>
     *   <li><b>Limited length:</b> Maximum 7 characters for URL brevity</li>
     *   <li><b>Nullable initially:</b> Populated after ID generation in Two-Step Save</li>
     * </ul>
     * <p>
     * The short code is the user-facing identifier used in shortened URLs.
     */
    @Column(name = "short_code", length = 7, unique = true)
    private String shortCode;

    /**
     * Timestamp indicating when this URL entity was first created.
     * <p>
     * This field is automatically populated by Hibernate using the
     * {@code @CreationTimestamp} annotation. It provides an immutable audit
     * trail for when URLs were shortened, useful for:
     * <ul>
     *   <li>Analytics and reporting</li>
     *   <li>Debugging and troubleshooting</li>
     *   <li>Data lifecycle management</li>
     * </ul>
     */
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}

