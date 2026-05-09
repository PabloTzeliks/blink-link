package pablo.tzeliks.blink_link.application.url.port.out;

import java.util.Optional;

public interface CachePort {

    void put(String shortCode, UrlContext payload, long ttl);

    Optional<String> get(String key);

    boolean exists(String key);

    void evict(String key);

    void putIfAbsent(String key, String value, long ttlInSeconds);

    Optional<UrlContext> getUrlContext(String shortCode);
}