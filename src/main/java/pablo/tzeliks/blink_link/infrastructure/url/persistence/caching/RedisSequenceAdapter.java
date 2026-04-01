package pablo.tzeliks.blink_link.infrastructure.url.persistence.caching;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import pablo.tzeliks.blink_link.application.url.exception.SequenceGenerationException;
import pablo.tzeliks.blink_link.application.url.ports.SequencePort;
import pablo.tzeliks.blink_link.domain.url.ports.UrlRepositoryPort;

@Slf4j
@Component
public class RedisSequenceAdapter implements SequencePort {

    @Value("${app.sequence.redis-key}")
    private String sequenceKey;

    @Value("${app.sequence.fallback-start}")
    private long fallbackStart;

    private final StringRedisTemplate redis;
    private final UrlRepositoryPort urlRepository;

    public RedisSequenceAdapter(StringRedisTemplate redis, UrlRepositoryPort urlRepository) {
        this.redis = redis;
        this.urlRepository = urlRepository;
    }

    @Override
    public Long nextId() {
        try {
            Long id = redis.opsForValue().increment(sequenceKey);

            if (id == null) {
                throw new SequenceGenerationException("Redis INCR returned null", null);
            }

            Long maxId = urlRepository.findMaxId();
            long safeFloor = maxId != null ? maxId : fallbackStart;

            if (id <= safeFloor) {
                log.warn("Sequence drift detected. Redis returned {}. PostgreSQL MAX(id) is {}. Resyncing.", id, safeFloor);

                id = resync(safeFloor);
            }

            return id;

        } catch (DataAccessException e) {
            throw new SequenceGenerationException("Redis unavailable for sequence generation", e);
        }
    }

    private Long resync(long safeFloor) {

        redis.opsForValue().set(sequenceKey, String.valueOf(safeFloor));
        Long id = redis.opsForValue().increment(sequenceKey);

        log.info("Sequence resynced. New ID: {}", id);

        return id;
    }
}
