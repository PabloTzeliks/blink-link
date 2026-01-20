package pablo.tzeliks.blink_link.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "url", schema = "public")
public class UrlEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "original_url", nullable = false)
    private String originalUrl;

    @Column(name = "short_code", length = 7, unique = true)
    private String shortCode;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Additional Constructor

    public UrlEntity(String originalUrl) {
        this.originalUrl = originalUrl;
    }
}
