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
                throw new SequenceGenerationException("Failed to generate sequence: returned null", null);
            }
            return id;

        } catch (DataAccessException e) {
            throw new SequenceGenerationException("Failed to retrieve next ID due to infrastructure unavailability", e);
        }
    }

    @Override
    public void resync() {
        try {
            Long maxId = urlRepository.findMaxId();
            long startFrom = maxId != null ? maxId : fallbackStart;

            redis.opsForValue().set(sequenceKey, String.valueOf(startFrom));

            log.info("Self-healing: Redis sequence resynchronized to {}", startFrom);
        } catch (Exception e) {
            log.error("Failed trying to resynchronize the Redis Sequence", e);
        }
    }
}
