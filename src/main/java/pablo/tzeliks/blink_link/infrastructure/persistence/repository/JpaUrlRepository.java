package pablo.tzeliks.blink_link.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pablo.tzeliks.blink_link.infrastructure.persistence.entity.UrlEntity;

import java.util.Optional;

/**
 * TODO : new Docs
 *
 * @author Pablo Tzeliks
 * @version 2.0.0
 * @since 1.0.0
 */
@Repository
public interface JpaUrlRepository extends JpaRepository<UrlEntity, Long> {

    Optional<UrlEntity> findByShortCode(String shortCode);

    @Query(value = "SELECT nextval('url_id_seq')", nativeQuery = true)
    Long nextId();
}

