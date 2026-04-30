package pablo.tzeliks.blink_link.infrastructure.url.persistence.caching;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import pablo.tzeliks.blink_link.infrastructure.AbstractContainerBase;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

    @Test
    @DisplayName("fallback: get returns empty and does not throw exception when Redis fails")
    void fallback_getReturnsEmptyWhenRedisFails() {
        // Arrange:
        StringRedisTemplate brokenRedisTemplate = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);

        when(brokenRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenThrow(new RedisConnectionFailureException("Connection refused"));

        RedisCacheAdapter fallbackAdapter = new RedisCacheAdapter(brokenRedisTemplate);

        // Act & Assert:
        assertThatCode(() -> {
            Optional<String> result = fallbackAdapter.get("xyz");
            assertThat(result).isEmpty();
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("fallback: put fails silently when Redis is down")
    void fallback_putFailsSilentlyWhenRedisIsDown() {
        // Arrange
        StringRedisTemplate brokenRedisTemplate = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);

        when(brokenRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(brokenRedisTemplate.opsForValue()).thenReturn(valueOperations);

        doThrow(new RuntimeException("Redis Timeout"))
                .when(valueOperations).set(anyString(), anyString(), anyLong(), any());

        RedisCacheAdapter fallbackAdapter = new RedisCacheAdapter(brokenRedisTemplate);

        // Act & Assert
        assertThatCode(() -> fallbackAdapter.put("abc", "https://example.com", 60L))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("fallback: evict fails silently when Redis is down")
    void fallback_evictFailsSilentlyWhenRedisIsDown() {
        // Arrange
        StringRedisTemplate brokenRedisTemplate = mock(StringRedisTemplate.class);
        when(brokenRedisTemplate.delete(anyString())).thenThrow(new RedisConnectionFailureException("Node unavailable"));

        RedisCacheAdapter fallbackAdapter = new RedisCacheAdapter(brokenRedisTemplate);

        // Act & Assert
        assertThatCode(() -> fallbackAdapter.evict("abc"))
                .doesNotThrowAnyException();
    }

    @Nested
    @DisplayName("exists()")
    class ExistsTests {

        @Test
        @DisplayName("Should return true when key exists in Redis")
        void shouldReturnTrue_whenKeyExists() {
            redisTemplate.opsForValue().set("url:present-key", "https://example.com");

            assertThat(cacheAdapter.exists("present-key")).isTrue();
        }

        @Test
        @DisplayName("Should return false when key does not exist in Redis")
        void shouldReturnFalse_whenKeyDoesNotExist() {
            assertThat(cacheAdapter.exists("missing-key")).isFalse();
        }

        @Test
        @DisplayName("Should return false after key TTL expires")
        void shouldReturnFalse_afterTtlExpires() throws InterruptedException {
            redisTemplate.opsForValue().set("url:expiring-key", "https://example.com", 1, SECONDS);

            Thread.sleep(1100);

            assertThat(cacheAdapter.exists("expiring-key")).isFalse();
        }
    }
}
