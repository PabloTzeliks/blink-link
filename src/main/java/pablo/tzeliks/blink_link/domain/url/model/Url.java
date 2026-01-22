package pablo.tzeliks.blink_link.domain.url.model;

import lombok.*;

import java.time.LocalDateTime;

/**
 * TODO : new Docs
 *
 * @author Pablo Tzeliks
 * @version 2.0.0
 * @since 2.0.0
 */
@Data
public class Url {

    private String originalUrl;
    private String shortCode;
    private LocalDateTime createdAt;

    // Additional Constructor

    public Url(String originalUrl) {
        this.originalUrl = originalUrl;
    }
}
