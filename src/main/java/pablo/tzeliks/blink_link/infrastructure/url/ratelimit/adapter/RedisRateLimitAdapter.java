package pablo.tzeliks.blink_link.infrastructure.url.ratelimit.adapter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import pablo.tzeliks.blink_link.application.url.port.out.RateLimitPort;
import pablo.tzeliks.blink_link.application.url.port.out.RateLimitResult;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Slf4j
@Component
public class RedisRateLimitAdapter implements RateLimitPort {

    private static final String KEY_PREFIX = "rate:";
    private static final long WINDOW_SECONDS = 60;

    private final StringRedisTemplate redis;
    private final RedisScript<List> script;
    private final Clock clock;

    @Autowired
    public RedisRateLimitAdapter(StringRedisTemplate redis, RedisScript<List> rateLimitScript) {
        this(redis, rateLimitScript, Clock.systemUTC());
    }

    // Visible for testing: lets tests inject a fixed Clock for deterministic windows.
    RedisRateLimitAdapter(StringRedisTemplate redis, RedisScript<List> rateLimitScript, Clock clock) {
        this.redis = redis;
        this.script = rateLimitScript;
        this.clock = clock;
    }

    @Override
    public RateLimitResult check(String ownerId, int limit) {

        try {
            long now = Instant.now(clock).getEpochSecond();
            long windowId = now / WINDOW_SECONDS;
            long elapsed = now % WINDOW_SECONDS;

            String currentKey  = KEY_PREFIX + ownerId + ":" + windowId;
            String previousKey = KEY_PREFIX + ownerId + ":" + (windowId - 1);

            List<Long> r = redis.execute(script,
                    List.of(currentKey, previousKey),
                    String.valueOf(limit), String.valueOf(WINDOW_SECONDS), String.valueOf(elapsed));

            boolean allowed = r.get(0) == 1L;
            long used = r.get(1);
            int remaining = (int) Math.max(0, limit - used);

            return new RateLimitResult(allowed, remaining, limit);
        } catch (Exception e) {

            log.warn("[RateLimit Fallback] Redis unavailable, releasing. owner={} reason={}", ownerId, e.getMessage());
            return new RateLimitResult(true, limit, limit);
        }
    }
}
