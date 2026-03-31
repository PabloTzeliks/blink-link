package pablo.tzeliks.blink_link.infrastructure.url.persistence.caching;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import pablo.tzeliks.blink_link.infrastructure.AbstractContainerBase;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RedisCacheAdapterIntegrationTest extends AbstractContainerBase {

    @Autowired
    private RedisCacheAdapter cacheAdapter;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void setUp() {
        redisTemplate.execute((RedisCallback<Object>) connection -> {
            connection.serverCommands().flushDb();
            return null;
        });
    }

    @Test
    @DisplayName("put stores value and TTL is applied")
    void put_storesValueAndTtlIsApplied() {
        cacheAdapter.put("abc", "https://example.com", 60L);

        assertThat(redisTemplate.opsForValue().get("url:abc")).isEqualTo("https://example.com");
        Long ttl = redisTemplate.getExpire("url:abc", TimeUnit.SECONDS);
        assertThat(ttl).isGreaterThan(0).isLessThanOrEqualTo(60);
    }

    @Test
    @DisplayName("get returns present value")
    void get_returnsPresentValue() {
        redisTemplate.opsForValue().set("url:xyz", "https://stored.com");

        Optional<String> result = cacheAdapter.get("xyz");

        assertThat(result).contains("https://stored.com");
    }

    @Test
    @DisplayName("get returns empty for missing key")
    void get_returnsEmptyForMissingKey() {
        Optional<String> result = cacheAdapter.get("nonexistent");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("evict removes the key")
    void evict_removesTheKey() {
        redisTemplate.opsForValue().set("url:del", "https://todelete.com");

        cacheAdapter.evict("del");

        assertThat(redisTemplate.opsForValue().get("url:del")).isNull();
    }

    @Test
    @DisplayName("putIfAbsent does not overwrite existing key")
    void putIfAbsent_doesNotOverwriteExistingKey() {
        redisTemplate.opsForValue().set("url:custom", "https://original.com");

        cacheAdapter.putIfAbsent("custom", "https://new.com", 60L);

        assertThat(redisTemplate.opsForValue().get("url:custom")).isEqualTo("https://original.com");
    }

    @Test
    @DisplayName("putIfAbsent writes when key is absent")
    void putIfAbsent_writesWhenKeyIsAbsent() {
        cacheAdapter.putIfAbsent("newcode", "https://example.com", 60L);

        assertThat(redisTemplate.opsForValue().get("url:newcode")).isEqualTo("https://example.com");
    }
}
