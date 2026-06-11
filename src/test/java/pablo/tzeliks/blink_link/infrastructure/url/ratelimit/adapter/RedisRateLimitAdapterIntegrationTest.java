package pablo.tzeliks.blink_link.infrastructure.url.ratelimit.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import pablo.tzeliks.blink_link.application.url.port.out.RateLimitResult;
import pablo.tzeliks.blink_link.infrastructure.AbstractContainerBase;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class RedisRateLimitAdapterIntegrationTest extends AbstractContainerBase {

    // Mirror of the adapter's private constants (integration coupling is acceptable here).
    private static final String KEY_PREFIX = "rate:";
    private static final long WINDOW_SECONDS = 60;

    @Autowired
    private StringRedisTemplate redis;

    @Autowired
    private RedisScript<List> rateLimitScript;

    @BeforeEach
    void setUp() {
        redis.execute((RedisCallback<Object>) connection -> {
            connection.serverCommands().flushDb();
            return null;
        });
    }

    /** Builds an adapter whose clock is frozen at a second with the given offset into the window. */
    private RedisRateLimitAdapter adapterAtElapsed(long elapsedSeconds) {
        long second = WINDOW_SECONDS * 28_000_000L + elapsedSeconds;
        Clock fixed = Clock.fixed(Instant.ofEpochSecond(second), ZoneOffset.UTC);
        return new RedisRateLimitAdapter(redis, rateLimitScript, fixed);
    }

    private static long windowIdFor(long elapsedSeconds) {
        return (WINDOW_SECONDS * 28_000_000L + elapsedSeconds) / WINDOW_SECONDS;
    }

    @Test
    @DisplayName("Allows requests under the limit and decrements remaining")
    void allowsUnderLimitAndDecrementsRemaining() {
        RedisRateLimitAdapter adapter = adapterAtElapsed(10);
        String owner = UUID.randomUUID().toString();

        RateLimitResult first = adapter.check(owner, 5);
        RateLimitResult second = adapter.check(owner, 5);

        assertThat(first.isAllowed()).isTrue();
        assertThat(first.remainingRequests()).isEqualTo(4);
        assertThat(second.isAllowed()).isTrue();
        assertThat(second.remainingRequests()).isEqualTo(3);
    }

    @Test
    @DisplayName("Blocks once the limit is reached within the window")
    void blocksWhenLimitReached() {
        RedisRateLimitAdapter adapter = adapterAtElapsed(10);
        String owner = UUID.randomUUID().toString();
        int limit = 3;

        assertThat(adapter.check(owner, limit).isAllowed()).isTrue();   // 1
        assertThat(adapter.check(owner, limit).isAllowed()).isTrue();   // 2
        assertThat(adapter.check(owner, limit).isAllowed()).isTrue();   // 3
        assertThat(adapter.check(owner, limit).isAllowed()).isFalse();  // 4 -> blocked
    }

    @Test
    @DisplayName("Sliding window: the previous window still counts against the current budget")
    void previousWindowCountsAgainstCurrentBudget() {
        // Frozen at 30s into the window -> previous window weight = (60-30)/60 = 0.5
        long elapsed = 30;
        RedisRateLimitAdapter adapter = adapterAtElapsed(elapsed);
        String owner = UUID.randomUUID().toString();

        // Seed the PREVIOUS window bucket with 10 hits.
        String previousKey = KEY_PREFIX + owner + ":" + (windowIdFor(elapsed) - 1);
        redis.opsForValue().set(previousKey, "10");

        // limit=10; previous contributes 10 * 0.5 = 5 -> only 5 current calls fit.
        // A naive FIXED window would ignore the previous window and allow all 10.
        int limit = 10;
        for (int i = 1; i <= 5; i++) {
            assertThat(adapter.check(owner, limit).isAllowed())
                    .as("call %d should be allowed", i)
                    .isTrue();
        }

        assertThat(adapter.check(owner, limit).isAllowed())
                .as("6th call blocked because the previous window's traffic still weighs in")
                .isFalse();
    }

    @Test
    @DisplayName("Fail-open: releases the request when Redis is unavailable")
    void failOpenWhenRedisDown() {
        StringRedisTemplate brokenRedis = mock(StringRedisTemplate.class);
        when(brokenRedis.execute(any(RedisScript.class), anyList(), any(), any(), any()))
                .thenThrow(new RedisConnectionFailureException("Connection refused"));

        RedisRateLimitAdapter adapter = new RedisRateLimitAdapter(brokenRedis, rateLimitScript, Clock.systemUTC());

        RateLimitResult result = adapter.check(UUID.randomUUID().toString(), 5);

        assertThat(result.isAllowed()).isTrue();
        assertThat(result.remainingRequests()).isEqualTo(5);
    }
}
