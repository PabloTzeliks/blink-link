package pablo.tzeliks.blink_link.infrastructure.persistence.repository;

import org.springframework.stereotype.Repository;
import pablo.tzeliks.blink_link.domain.url.model.Url;
import pablo.tzeliks.blink_link.domain.url.ports.UrlRepositoryPort;
import pablo.tzeliks.blink_link.infrastructure.exception.PersistenceException;
import pablo.tzeliks.blink_link.infrastructure.persistence.mapper.UrlEntityMapper;

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
    private final UrlEntityMapper mapper;

    public PostgresUrlRepository(JpaUrlRepository repository, UrlEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Long nextId() {

        return repository.nextId();
    }

    @Override
    public Url save(Url url) {

        return mapper.toDomain(repository.save(mapper.toEntity(url)));
    }

    @Override
    public Optional<Url> findById(Long id) {

        return repository.findById(id)
                .map(mapper::toDomain)
                .orElse(new PersistenceException("Cannot find an URL for the ID " + id));
    }

    @Override
    public Optional<Url> findByShortCode(String shortCode) {

        return repository.findByShortCode(shortCode)
                .map(mapper::toDomain)
                .orElse(new PersistenceException("Cannot find an URL for the ShortCode " + shortCode));
    }
}
