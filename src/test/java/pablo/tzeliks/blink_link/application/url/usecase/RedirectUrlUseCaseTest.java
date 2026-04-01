package pablo.tzeliks.blink_link.application.url.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import pablo.tzeliks.blink_link.application.url.dto.ResolveUrlRequest;
import pablo.tzeliks.blink_link.application.url.dto.UrlResponse;
import pablo.tzeliks.blink_link.application.url.mapper.UrlDtoMapper;
import pablo.tzeliks.blink_link.application.url.ports.CachePort;
import pablo.tzeliks.blink_link.domain.url.exception.UrlExpiredException;
import pablo.tzeliks.blink_link.domain.url.model.Url;
import pablo.tzeliks.blink_link.domain.url.ports.UrlRepositoryPort;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RedirectUrlUseCaseTest {

    @Mock
    private UrlRepositoryPort repository;

    @Mock
    private CachePort cache;

    @Mock
    private UrlDtoMapper mapper;

    private RedirectUrlUseCase useCase;

    private static final long MAX_CACHE_TTL_SECONDS = 604800L;

    @BeforeEach
    void setUp() {
        useCase = new RedirectUrlUseCase(repository, cache);
        ReflectionTestUtils.setField(useCase, "maxCacheTtlSeconds", MAX_CACHE_TTL_SECONDS);
    }

    @Test
    @DisplayName("Should correctly implement Cache Hit when url is in Cache")
    void shouldImplementCacheHit() {

        // Arrange
        String shortCode = "HhqS";
        String originalUrl = "https://github.com/PabloTzeliks";

        ResolveUrlRequest request = new ResolveUrlRequest(shortCode);
        UrlResponse expectedResponse = new UrlResponse(originalUrl);

        // 1. Cache Hit
        when(cache.get(shortCode)).thenReturn(Optional.of(originalUrl));

        // Act
        UrlResponse actualResponse = useCase.execute(request);

        // Assert
        assertNotNull(actualResponse);
        assertEquals(originalUrl, actualResponse.originalUrl());
        verify(cache).get(shortCode);
        verify(repository, never()).findByShortCode(any());
    }

    @Test
    @DisplayName("Should correctly implement Cache Miss when url is not on Cache")
    void shouldImplementCacheMiss() {

        // Arrange
        String shortCode = "HhqS";
        String originalUrl = "https://github.com/PabloTzeliks";
        LocalDateTime now = LocalDateTime.now();
        UUID userId = UUID.randomUUID();
        Url urlFound = Url.restore(1L, userId, originalUrl, shortCode, now, now.plusDays(7));

        ResolveUrlRequest request = new ResolveUrlRequest(shortCode);
        UrlResponse expectedResponse = new UrlResponse(originalUrl);

        // 1. Cache Miss
        when(cache.get(shortCode)).thenReturn(Optional.empty());

        // 2. PostgreSQL search
        when(repository.findByShortCode(shortCode)).thenReturn(Optional.of(urlFound));

        // Act
        UrlResponse actualResponse = useCase.execute(request);

        // Assert
        assertNotNull(actualResponse);
        assertEquals(expectedResponse.originalUrl(), actualResponse.originalUrl());

        verify(cache).get(shortCode);
        verify(repository).findByShortCode(shortCode);
    }

    @Test
    @DisplayName("Should correctly implement add Url on Cache after Cache Miss")
    void shouldImplementCachingWhenCacheMiss() {
        String shortCode = "HhqS";
        String originalUrl = "https://github.com/PabloTzeliks";
        LocalDateTime now = LocalDateTime.now();
        UUID userId = UUID.randomUUID();
        Url urlFound = Url.restore(1L, userId, originalUrl, shortCode, now, now.plusDays(7));

        long expectedTtl = Math.min(urlFound.getSecondsUntilExpiry(), MAX_CACHE_TTL_SECONDS);

        when(cache.get(shortCode)).thenReturn(Optional.empty());
        when(repository.findByShortCode(shortCode)).thenReturn(Optional.of(urlFound));

        useCase.execute(new ResolveUrlRequest(shortCode));

        verify(cache).get(shortCode);
        verify(repository).findByShortCode(shortCode);
        verify(cache).put(
                eq("HhqS"),
                eq("https://github.com/PabloTzeliks"),
                longThat(ttl -> ttl >= 604795L && ttl <= 604800L)
        );
    }

    @Test
    @DisplayName("Should throw UrlExpiredException and not cache expired URL")
    void shouldThrowWhenUrlIsExpired() {
        String shortCode = "HhqS";
        LocalDateTime past = LocalDateTime.now().minusDays(1);
        UUID userId = UUID.randomUUID();
        Url expiredUrl = Url.restore(1L, userId, "https://example.com", shortCode, past.minusDays(7), past);

        when(cache.get(shortCode)).thenReturn(Optional.empty());
        when(repository.findByShortCode(shortCode)).thenReturn(Optional.of(expiredUrl));

        assertThrows(UrlExpiredException.class,
                () -> useCase.execute(new ResolveUrlRequest(shortCode)));

        verify(cache, never()).put(any(), any(), anyLong());
    }
}
