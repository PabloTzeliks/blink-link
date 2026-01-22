package pablo.tzeliks.blink_link.infraestructure.persistence.repository;

import org.springframework.stereotype.Repository;
import pablo.tzeliks.blink_link.domain.url.model.Url;
import pablo.tzeliks.blink_link.domain.url.ports.UrlRepositoryPort;

import java.util.Optional;

/**
 * TODO : new Docs
 *
 * @author Pablo Tzeliks
 * @version 2.0.0
 * @since 2.0.0
 */
@Repository
public class PostgresUrlRepository implements UrlRepositoryPort {

    private final JpaUrlRepository repository;

    public PostgresUrlRepository(JpaUrlRepository repository) {
        this.repository = repository;
    }

    @Override
    public Url save(Url url) {

    }

    @Override
    public Url findById(Long id) {

    }

    @Override
    public Optional<Url> findByShortCode(String shortCode) {

    }
}
