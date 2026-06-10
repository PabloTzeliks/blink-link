package pablo.tzeliks.blink_link.infrastructure.url.caching.adapter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import pablo.tzeliks.blink_link.application.url.port.out.CachePort;
import pablo.tzeliks.blink_link.application.url.port.out.UrlContext;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RedisCacheAdapter implements CachePort {

    private static final String KEY_PREFIX = "url:";

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    public RedisCacheAdapter(StringRedisTemplate redis, ObjectMapper objectMapper) {
        this.redis = redis;
        this.objectMapper = objectMapper;
    }

    @Override
    public void put(String key, UrlContext payload, long ttl) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            redis.opsForValue().set(KEY_PREFIX + key, json, ttl, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("[Redis Fallback] Fail on saving (put). Key: {}. Reason: {}", key, e.getMessage());
        }
    }

    @Override
    public Optional<UrlContext> getUrlContext(String key) {
        try {
            String json = redis.opsForValue().get(KEY_PREFIX + key);

            if (json == null) {
                return Optional.empty();
            }

            UrlContext context = objectMapper.readValue(json, UrlContext.class);

            return Optional.of(context);
        } catch (Exception e) {
            log.warn("[Redis Fallback] Fail on searching (getUrlContext). Simulates Cache Miss for Key: {}. Reason: {}", key, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public boolean exists(String key) {
        try {
            return Boolean.TRUE.equals(redis.hasKey(KEY_PREFIX + key));
        } catch (Exception e) {
            log.warn("[Redis Fallback] Fail on checking existence (exists). Key: {}. Reason: {}", key, e.getMessage());
            return false;
        }
    }

    @Override
    public void evict(String key) {
        try {
            redis.delete(KEY_PREFIX + key);
        } catch (Exception e) {
            log.warn("[Redis Fallback] Fail on removing (evict). Key: {}. Reason: {}", key, e.getMessage());
        }
    }
}
