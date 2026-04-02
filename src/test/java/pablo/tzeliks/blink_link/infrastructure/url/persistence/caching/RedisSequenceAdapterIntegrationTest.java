package pablo.tzeliks.blink_link.infrastructure.url.persistence.caching;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import pablo.tzeliks.blink_link.application.url.exception.SequenceGenerationException;
import pablo.tzeliks.blink_link.domain.url.model.Url;
import pablo.tzeliks.blink_link.domain.url.ports.UrlRepositoryPort;
import pablo.tzeliks.blink_link.domain.user.model.AuthProvider;
import pablo.tzeliks.blink_link.domain.user.model.Plan;
import pablo.tzeliks.blink_link.domain.user.model.Role;
import pablo.tzeliks.blink_link.infrastructure.AbstractContainerBase;
import pablo.tzeliks.blink_link.infrastructure.user.persistence.entity.UserEntity;
import pablo.tzeliks.blink_link.infrastructure.user.persistence.repository.JpaUserRepository;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
class RedisSequenceAdapterIntegrationTest extends AbstractContainerBase {

    @Autowired
    private RedisSequenceAdapter adapter;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private UrlRepositoryPort urlRepository;

    @Autowired
    private JpaUserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("TRUNCATE TABLE urls, users CASCADE");

        redisTemplate.getConnectionFactory().getConnection().flushDb();
        redisTemplate.opsForValue().set("sequence:url:id", "1000000");
    }

    @Test
    @DisplayName("Should increment and return next ID")
    void shouldIncrementAndReturnNextId() {
        Long nextId = adapter.nextId();

        assertEquals(1000001L, nextId);
        assertEquals("1000001", redisTemplate.opsForValue().get("sequence:url:id"));
    }

    @Test
    @DisplayName("Should return sequential IDs")
    void shouldReturnSequentialIds() {
        Long first = adapter.nextId();
        Long second = adapter.nextId();
        Long third = adapter.nextId();

        assertEquals(1000001L, first);
        assertEquals(1000002L, second);
        assertEquals(1000003L, third);
    }

    @Test
    @DisplayName("Should detect drift and resync after Redis restart with clean state")
    void shouldDetectDriftAndResync() {
        saveUrl(1000020L, "drf201");
        redisTemplate.getConnectionFactory().getConnection().flushDb();

        Long corrected = adapter.nextId();
        Long fallbackStart = 999999L;

        assertTrue(corrected > fallbackStart);
        assertEquals(String.valueOf(corrected), redisTemplate.opsForValue().get("sequence:url:id"));
    }

    @Test
    @DisplayName("Should detect drift when Redis is behind PostgreSQL")
    void shouldDetectDriftWhenRedisBehindPostgres() {
        saveUrl(1000030L, "drf301");
        Long postgresMaxBefore = urlRepository.findMaxId();
        redisTemplate.opsForValue().set("sequence:url:id", "5");

        Long corrected = adapter.nextId();

        assertTrue(corrected > postgresMaxBefore);
        assertEquals(String.valueOf(corrected), redisTemplate.opsForValue().get("sequence:url:id"));
    }

    @Test
    @DisplayName("Should throw SequenceGenerationException when Redis is unavailable")
    void shouldThrowSequenceGenerationExceptionWhenRedisUnavailable() {
        StringRedisTemplate brokenRedis = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        when(brokenRedis.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment(anyString())).thenThrow(new org.springframework.data.redis.RedisConnectionFailureException("Redis unavailable"));

        RedisSequenceAdapter brokenAdapter = new RedisSequenceAdapter(brokenRedis, urlRepository);
        ReflectionTestUtils.setField(brokenAdapter, "sequenceKey", "sequence:url:id");
        ReflectionTestUtils.setField(brokenAdapter, "fallbackStart", 999999L);

        assertThrows(SequenceGenerationException.class, brokenAdapter::nextId);
    }

    private void saveUrl(Long id, String shortCode) {
        UUID userId = UUID.randomUUID();
        userRepository.saveAndFlush(new UserEntity(
                userId,
                "redis-seq-" + shortCode + "@test.com",
                "encoded",
                Role.USER,
                Plan.FREE,
                AuthProvider.LOCAL,
                LocalDateTime.now(),
                LocalDateTime.now()
        ));

        Url url = Url.restore(
                id,
                userId,
                "https://example.com/" + shortCode,
                shortCode,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(7)
        );

        urlRepository.save(url);
    }
}
