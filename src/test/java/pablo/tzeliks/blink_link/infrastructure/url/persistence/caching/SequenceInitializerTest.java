package pablo.tzeliks.blink_link.infrastructure.url.persistence.caching;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;
import pablo.tzeliks.blink_link.domain.url.ports.UrlRepositoryPort;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SequenceInitializerTest {

    @Mock
    private StringRedisTemplate redis;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private UrlRepositoryPort urlRepository;

    private SequenceInitializer initializer;

    @BeforeEach
    void setUp() {
        initializer = new SequenceInitializer(redis, urlRepository);
        ReflectionTestUtils.setField(initializer, "sequenceKey", "sequence:url:id");
        ReflectionTestUtils.setField(initializer, "fallbackStart", 999999L);
        when(redis.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("Should initialize sequence from PostgreSQL max ID")
    void shouldInitializeSequenceFromPostgresMaxId() {
        when(urlRepository.findMaxId()).thenReturn(1000050L);
        when(valueOperations.setIfAbsent("sequence:url:id", "1000050")).thenReturn(true);

        initializer.initialize();

        verify(valueOperations).setIfAbsent("sequence:url:id", "1000050");
    }

    @Test
    @DisplayName("Should use fallback when PostgreSQL is empty")
    void shouldUseFallbackWhenPostgresIsEmpty() {
        when(urlRepository.findMaxId()).thenReturn(null);
        when(valueOperations.setIfAbsent("sequence:url:id", "999999")).thenReturn(true);

        initializer.initialize();

        verify(valueOperations).setIfAbsent("sequence:url:id", "999999");
    }

    @Test
    @DisplayName("Should skip initialization when key already exists")
    void shouldSkipInitializationWhenKeyAlreadyExists() {
        when(urlRepository.findMaxId()).thenReturn(1000050L);
        when(valueOperations.setIfAbsent("sequence:url:id", "1000050")).thenReturn(false);

        initializer.initialize();

        verify(valueOperations, times(1)).setIfAbsent("sequence:url:id", "1000050");
        verifyNoMoreInteractions(valueOperations);
    }
}
