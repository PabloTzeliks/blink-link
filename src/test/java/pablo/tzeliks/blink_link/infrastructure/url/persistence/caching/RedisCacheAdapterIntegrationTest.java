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
import pablo.tzeliks.blink_link.application.url.port.out.UrlContext;
import pablo.tzeliks.blink_link.infrastructure.AbstractContainerBase;
import pablo.tzeliks.blink_link.infrastructure.url.caching.adapter.RedisCacheAdapter;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;
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

    @Autowired
    private ObjectMapper objectMapper;

    private static String ownerId() {
        return UUID.randomUUID().toString();
    }

    @BeforeEach
    void setUp() {
        redisTemplate.execute((RedisCallback<Object>) connection -> {
            connection.serverCommands().flushDb();
            return null;
        });
    }

    @Test
    @DisplayName("put stores the UrlContext and TTL is applied")
    void put_storesValueAndTtlIsApplied() {
        UrlContext context = new UrlContext("https://example.com", ownerId(), 100);

        cacheAdapter.put("abc", context, 60L);

        assertThat(cacheAdapter.getUrlContext("abc")).contains(context);
        Long ttl = redisTemplate.getExpire("url:abc", TimeUnit.SECONDS);
        assertThat(ttl).isGreaterThan(0).isLessThanOrEqualTo(60);
    }

    @Test
    @DisplayName("getUrlContext returns present value")
    void getUrlContext_returnsPresentValue() {
        UrlContext context = new UrlContext("https://stored.com", ownerId(), 500);
        cacheAdapter.put("xyz", context, 60L);

        assertThat(cacheAdapter.getUrlContext("xyz")).contains(context);
    }

    @Test
    @DisplayName("getUrlContext returns empty for missing key")
    void getUrlContext_returnsEmptyForMissingKey() {
        assertThat(cacheAdapter.getUrlContext("nonexistent")).isEmpty();
    }

    @Test
    @DisplayName("getUrlContext returns empty for corrupted (non-JSON) value")
    void getUrlContext_returnsEmptyForCorruptedValue() {
        redisTemplate.opsForValue().set("url:broken", "not-a-json-payload");

        assertThat(cacheAdapter.getUrlContext("broken")).isEmpty();
    }

    @Test
    @DisplayName("evict removes the key")
    void evict_removesTheKey() {
        cacheAdapter.put("del", new UrlContext("https://todelete.com", ownerId(), 100), 60L);

        cacheAdapter.evict("del");

        assertThat(cacheAdapter.getUrlContext("del")).isEmpty();
    }

    @Test
    @DisplayName("fallback: getUrlContext returns empty and does not throw when Redis fails")
    void fallback_getReturnsEmptyWhenRedisFails() {
        // Arrange
        StringRedisTemplate brokenRedisTemplate = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);

        when(brokenRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenThrow(new RedisConnectionFailureException("Connection refused"));

        RedisCacheAdapter fallbackAdapter = new RedisCacheAdapter(brokenRedisTemplate, objectMapper);

        // Act & Assert
        assertThatCode(() ->
                assertThat(fallbackAdapter.getUrlContext("xyz")).isEmpty()
        ).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("fallback: put fails silently when Redis is down")
    void fallback_putFailsSilentlyWhenRedisIsDown() {
        // Arrange
        StringRedisTemplate brokenRedisTemplate = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);

        when(brokenRedisTemplate.opsForValue()).thenReturn(valueOperations);
        doThrow(new RuntimeException("Redis Timeout"))
                .when(valueOperations).set(anyString(), anyString(), anyLong(), any());

        RedisCacheAdapter fallbackAdapter = new RedisCacheAdapter(brokenRedisTemplate, objectMapper);

        // Act & Assert
        assertThatCode(() ->
                fallbackAdapter.put("abc", new UrlContext("https://example.com", ownerId(), 100), 60L)
        ).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("fallback: evict fails silently when Redis is down")
    void fallback_evictFailsSilentlyWhenRedisIsDown() {
        // Arrange
        StringRedisTemplate brokenRedisTemplate = mock(StringRedisTemplate.class);
        when(brokenRedisTemplate.delete(anyString())).thenThrow(new RedisConnectionFailureException("Node unavailable"));

        RedisCacheAdapter fallbackAdapter = new RedisCacheAdapter(brokenRedisTemplate, objectMapper);

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
