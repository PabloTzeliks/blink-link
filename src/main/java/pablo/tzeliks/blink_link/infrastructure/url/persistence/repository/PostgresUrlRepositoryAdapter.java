package pablo.tzeliks.blink_link.infrastructure.url.persistence.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pablo.tzeliks.blink_link.domain.url.model.Url;
import pablo.tzeliks.blink_link.domain.url.ports.UrlRepositoryPort;
import pablo.tzeliks.blink_link.infrastructure.url.persistence.mapper.UrlEntityMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author Pablo Tzeliks
 * @version 2.0.0
 * @since 2.0.0
 */
@Repository
public class PostgresUrlRepositoryAdapter implements UrlRepositoryPort {

    private final JpaUrlRepository repository;
    private final UrlEntityMapper mapper;
    private final EntityManager entityManager;

    public PostgresUrlRepositoryAdapter(JpaUrlRepository repository,
                                        UrlEntityMapper mapper,
                                        EntityManager entityManager) {
        this.repository = repository;
        this.mapper = mapper;
        this.entityManager = entityManager;
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
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Url> findByShortCode(String shortCode) {

        return repository.findByShortCode(shortCode)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional
    public List<String> deleteExpiredInBatchReturningCodes(LocalDateTime referenceTime, int batchSize) {

        String sql = """
        DELETE FROM urls
        WHERE id IN (
            SELECT id FROM urls
            WHERE expiration_date < :now
            ORDER BY id
            FOR UPDATE SKIP LOCKED
            LIMIT :batchSize
        )
        RETURNING short_code
    """;

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("now", referenceTime);
        query.setParameter("batchSize", batchSize);

        return query.getResultList();
    }
}
