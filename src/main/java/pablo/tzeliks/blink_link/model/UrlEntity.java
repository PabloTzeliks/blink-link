package pablo.tzeliks.blink_link.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * JPA entity representing a URL mapping in the URL shortening system.
 * <p>
 * This entity stores the relationship between original long URLs and their
 * shortened versions, along with metadata for tracking and auditing purposes.
 * <p>
 * <b>Key Design Decision - ID and Short Code Relationship:</b>
 * The {@code id} field serves as the primary key and is the source of truth for
 * uniqueness. The {@code shortCode} is <b>derived</b> from the ID through Base62
 * encoding, not generated independently. This design ensures:
 * <ul>
 *   <li>Guaranteed uniqueness (leverages database ID generation)</li>
 *   <li>Collision-free codes (no need for retry logic)</li>
 *   <li>Predictable code length based on ID magnitude</li>
 *   <li>Bidirectional conversion between ID and short code</li>
 * </ul>
 * <p>
 * <b>Two-Step Save Pattern:</b>
 * This entity is saved in two phases:
 * <ol>
 *   <li>Initial save with only {@code originalUrl} populated</li>
 *   <li>Database generates the {@code id} (using IDENTITY strategy)</li>
 *   <li>Short code is computed from the ID and set on the entity</li>
 *   <li>JPA dirty checking persists the short code automatically</li>
 * </ol>
 * <p>
 * <b>Audit Trail:</b> The {@code createdAt} field is automatically populated
 * using {@code @CreationTimestamp} annotation, providing an immutable timestamp
 * of when each URL was first shortened. This supports analytics and debugging.
 * <p>
 * The entity uses Lombok annotations ({@code @Data}, {@code @NoArgsConstructor})
 * to automatically generate getters, setters, equals, hashCode, and toString methods,
 * reducing boilerplate code.
 *
 * @author Pablo Tzeliks
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

    // Additional Constructor

    /**
     * Constructs a new UrlEntity with the specified original URL.
     * <p>
     * This constructor is used during the Two-Step Save process to create
     * an entity that will be saved to obtain an ID, after which the short
     * code can be generated and set.
     *
     * @param originalUrl the long URL to be shortened; must not be null
     */
    public UrlEntity(String originalUrl) {
        this.originalUrl = originalUrl;
    }
}
}
