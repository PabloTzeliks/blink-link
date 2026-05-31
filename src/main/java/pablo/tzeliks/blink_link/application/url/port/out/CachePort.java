package pablo.tzeliks.blink_link.application.url.port.out;

import java.util.Optional;

public interface CachePort {

    void put(String shortCode, UrlContext payload, long ttl);

    Optional<UrlContext> getUrlContext(String shortCode);

    boolean exists(String key);

    void evict(String key);
}