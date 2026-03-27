package pablo.tzeliks.blink_link.infrastructure.url.persistence.caching;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import pablo.tzeliks.blink_link.application.url.ports.CachePort;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
public class RedisCacheAdapter implements CachePort {

    private static final String KEY_PREFIX = "url:";

    private final StringRedisTemplate redis;

    public RedisCacheAdapter(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public void put(String key, String value, long ttl) {
        redis.opsForValue().set(KEY_PREFIX + key, value, ttl, TimeUnit.SECONDS);
    }

    @Override
    public Optional<String> get(String key) {
        String value = redis.opsForValue().get(KEY_PREFIX + key);
        return Optional.ofNullable(value);
    }

    @Override
    public void evict(String key) {
        redis.delete(KEY_PREFIX + key);
    }

    @Override
    public void putIfAbsent(String key, String value, long ttl) {
        redis.opsForValue().setIfAbsent(KEY_PREFIX + key, value, ttl, TimeUnit.SECONDS);
    }
}
