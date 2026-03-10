package pablo.tzeliks.blink_link.infrastructure.url.persistence.mapper;

import org.springframework.stereotype.Component;
import pablo.tzeliks.blink_link.domain.url.model.Url;
import pablo.tzeliks.blink_link.infrastructure.url.persistence.entity.UrlEntity;

/**
 *
 * @author Pablo Tzeliks
 * @version 3.0.0
 * @since 1.0.0
 */
@Component
public class UrlEntityMapper {

    /**
     * Converts a domain {@link Url} object to a JPA {@link UrlEntity}.
     * <p>
     * This method is used before persistence operations to transform the
     * domain model into an entity that can be saved by JPA/Hibernate.
     * <p>
     * <b>Note:</b> The {@code createdAt} timestamp provided by the domain
     * object will be overwritten by Hibernate's {@code @CreationTimestamp}
     * during the actual persistence operation.
     *
     * @param domain the domain object to convert
     * @return a new {@link UrlEntity} with fields populated from the domain object
     */
    public UrlEntity toEntity(Url domain) {

        return new UrlEntity(
                domain.getId(),
                domain.getOriginalUrl(),
                domain.getShortCode(),
                domain.getCreatedAt(),
                domain.getExpirationDate()
        );
    }

    /**
     * Converts a JPA {@link UrlEntity} to a domain {@link Url} object.
     * <p>
     * This method is used after database queries to transform the JPA entity
     * into a domain model that can be used in business logic.
     * <p>
     * The domain object's constructor performs validation, ensuring that
     * only valid URLs exist in the domain layer even if the database
     * contains invalid data (which should not happen with proper constraints).
     *
     * @param entity the JPA entity to convert
     * @return a new {@link Url} domain object with fields populated from the entity
     */
    public Url toDomain(UrlEntity entity) {

        return Url.restore(
                entity.getId(),
                entity.getOriginalUrl(),
                entity.getShortCode(),
                entity.getCreatedAt(),
                entity.getExpirationDate()
        );
    }
}
