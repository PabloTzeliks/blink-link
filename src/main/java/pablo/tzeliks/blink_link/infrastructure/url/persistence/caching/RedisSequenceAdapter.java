package pablo.tzeliks.blink_link.infrastructure.url.persistence.caching;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import pablo.tzeliks.blink_link.application.url.ports.SequencePort;
import pablo.tzeliks.blink_link.infrastructure.exception.InfraestructureException;

@Component
public class RedisSequenceAdapter implements SequencePort {

    @Value("${app.sequence.redis-key}")
    private String sequenceKey;

    private final StringRedisTemplate redis;

    public RedisSequenceAdapter(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public Long nextId() {

        Long id = redis.opsForValue().increment(sequenceKey);
        if (id == null) {
            throw new InfraestructureException("Redis sequence returned null. Redis may be unavailable.");
        }

        return id;
    }
}
