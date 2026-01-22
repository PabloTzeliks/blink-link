package pablo.tzeliks.blink_link.infraestructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pablo.tzeliks.blink_link.infraestructure.persistence.entity.UrlEntity;

/**
 * TODO : new Docs
 *
 * @author Pablo Tzeliks
 * @version 2.0.0
 * @since 1.0.0
 */
@Repository
public interface JpaUrlRepository extends JpaRepository<UrlEntity, Long> {
}

