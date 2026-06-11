package pablo.tzeliks.blink_link.infrastructure.url.persistence.mapper;

import org.springframework.stereotype.Component;
import pablo.tzeliks.blink_link.domain.url.model.Url;
import pablo.tzeliks.blink_link.infrastructure.url.persistence.entity.UrlEntity;

/**
 * @author Pablo Tzeliks
 * @version 3.0.0
 * @since 1.0.0
 */
@Component
public class UrlEntityMapper {

    public UrlEntity toEntity(Url domain) {

        return new UrlEntity(
                domain.getId(),
                domain.getUserId(),
                domain.getOriginalUrl(),
                domain.getShortCode(),
                domain.getCreatedAt(),
                domain.getExpirationDate()
        );
    }

    public Url toDomain(UrlEntity entity) {

        return Url.restore(
                entity.getId(),
                entity.getUserId(),
                entity.getOriginalUrl(),
                entity.getShortCode(),
                entity.getCreatedAt(),
                entity.getExpirationDate()
        );
    }
}
