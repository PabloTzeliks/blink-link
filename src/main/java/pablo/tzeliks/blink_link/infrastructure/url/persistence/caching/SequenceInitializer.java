package pablo.tzeliks.blink_link.infrastructure.url.persistence.caching;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import pablo.tzeliks.blink_link.domain.url.ports.UrlRepositoryPort;

@Component
@Slf4j
public class SequenceInitializer {

    private final StringRedisTemplate redis;
    private final UrlRepositoryPort urlRepository;

    @Value("${app.sequence.redis-key}")
    private String sequenceKey;

    @Value("${app.sequence.fallback-start}")
    private long fallbackStart;

    public SequenceInitializer(StringRedisTemplate redis, UrlRepositoryPort urlRepository) {
        this.redis = redis;
        this.urlRepository = urlRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initialize() {

        Long maxId = urlRepository.findMaxId();
        long startFrom = Math.max(maxId != null ? maxId : 0L, fallbackStart);

        Boolean set = redis.opsForValue().setIfAbsent(sequenceKey, String.valueOf(startFrom));

        if (Boolean.TRUE.equals(set)) {
            log.info("Sequence initialized at {} from PostgreSQL MAX(id)", startFrom);
        } else {
            log.info("Sequence key already exists — another instance initialized first. Skipping.");
        }
    }
}
