package pablo.tzeliks.blink_link.infrastructure.url.persistence.caching;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import pablo.tzeliks.blink_link.application.url.ports.CachePort;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RedisCacheAdapter implements CachePort {

    private static final String KEY_PREFIX = "url:";

    private final StringRedisTemplate redis;

    public RedisCacheAdapter(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public void put(String key, String value, long ttl) {
        try {
            redis.opsForValue().set(KEY_PREFIX + key, value, ttl, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("[Redis Fallback] Fail on saving (put). Key: {}. Reason: {}", key, e.getMessage());
        }
    }

    @Override
    public Optional<String> get(String key) {
        try {
            String value = redis.opsForValue().get(KEY_PREFIX + key);
            return Optional.ofNullable(value);
        } catch (Exception e) {
            log.warn("[Redis Fallback] Fail on searching (get). Simulates Cache Miss for Key: {}. Reason: {}", key, e.getMessage());
            return Optional.empty();
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

    @Override
    public void putIfAbsent(String key, String value, long ttl) {
        try {
            redis.opsForValue().setIfAbsent(KEY_PREFIX + key, value, ttl, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("[Redis Fallback] Fail on saving (putIfAbsent). Key: {}. Reason: {}", key, e.getMessage());
        }
    }
}
