package pablo.tzeliks.blink_link.infrastructure.url.persistence.caching;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import pablo.tzeliks.blink_link.application.url.exception.SequenceGenerationException;
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

        try {
            Long id = redis.opsForValue().increment(sequenceKey);

            if (id == null) {
                throw new SequenceGenerationException("Failed to generate sequence: returned null", null);
            }
            return id;

        } catch (RedisConnectionFailureException | RedisSystemException e) {
            throw new SequenceGenerationException("Failed to retrieve next ID due to infrastructure unavailability", e);
        }
    }
}
