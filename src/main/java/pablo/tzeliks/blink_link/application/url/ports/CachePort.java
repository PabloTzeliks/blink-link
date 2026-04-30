package pablo.tzeliks.blink_link.application.url.ports;

import java.util.Optional;

public interface CachePort {

    void put(String key, String value, long ttl);

    Optional<String> get(String key);

    boolean exists(String key);

    void evict(String key);

    void putIfAbsent(String key, String value, long ttlInSeconds);
}