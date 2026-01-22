package pablo.tzeliks.blink_link.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;
import pablo.tzeliks.blink_link.domain.url.model.Url;
import pablo.tzeliks.blink_link.infrastructure.persistence.entity.UrlEntity;

@Component
public class UrlEntityMapper {

    public UrlEntity toEntity(Url domain) {

        return new UrlEntity(
                domain.getId(),
                domain.getOriginalUrl(),
                domain.getShortCode(),
                domain.getCreatedAt()
        );
    }

    public Url toDomain(UrlEntity entity) {

        return new Url(
                entity.getId(),
                entity.getOriginalUrl(),
                entity.getShortCode(),
                entity.getCreatedAt()
        );
    }
}
