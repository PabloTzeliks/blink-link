package pablo.tzeliks.blink_link.infrastructure.url.persistence.caching;

import pablo.tzeliks.blink_link.application.url.ports.CachePort;

import java.time.Duration;
import java.util.Optional;

public class RedisCacheAdapter implements CachePort {

    @Override
    public void put(String key, String value, Duration ttl) {

    }

    @Override
    public Optional<String> get(String key) {
        return Optional.empty();
    }

    @Override
    public void evict(String key) {

    }

    @Override
    public void putIfAbsent(String key, String value, Duration ttl) {

    }
}
