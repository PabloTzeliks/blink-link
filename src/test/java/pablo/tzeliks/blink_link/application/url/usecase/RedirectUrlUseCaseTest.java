package pablo.tzeliks.blink_link.application.url.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import pablo.tzeliks.blink_link.application.url.dto.ResolveShortCodeRequest;
import pablo.tzeliks.blink_link.application.url.dto.UrlResponse;
import pablo.tzeliks.blink_link.application.url.exception.RateLimitExceededException;
import pablo.tzeliks.blink_link.application.url.port.out.CachePort;
import pablo.tzeliks.blink_link.application.url.port.out.RateLimitPort;
import pablo.tzeliks.blink_link.application.url.port.out.RateLimitResult;
import pablo.tzeliks.blink_link.application.url.port.out.UrlContext;
import pablo.tzeliks.blink_link.domain.url.exception.UrlExpiredException;
import pablo.tzeliks.blink_link.domain.url.model.Url;
import pablo.tzeliks.blink_link.domain.url.ports.UrlRepositoryPort;
import pablo.tzeliks.blink_link.domain.user.model.Plan;
import pablo.tzeliks.blink_link.domain.user.model.User;
import pablo.tzeliks.blink_link.domain.user.ports.UserRepositoryPort;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RedirectUrlUseCaseTest {

    @Mock
    private UrlRepositoryPort repository;

    @Mock
    private UserRepositoryPort userRepository;

    @Mock
    private RateLimitPort rateLimit;

    @Mock
    private CachePort cache;

    private RedirectUrlUseCase useCase;

    private static final long MAX_CACHE_TTL_SECONDS = 604800L;

    @BeforeEach
    void setUp() {
        useCase = new RedirectUrlUseCase(repository, userRepository, rateLimit, cache);
        ReflectionTestUtils.setField(useCase, "maxCacheTtlSeconds", MAX_CACHE_TTL_SECONDS);
    }

    @Test
    @DisplayName("Should correctly implement Cache Hit when url is in Cache")
    void shouldImplementCacheHit() {

        // Arrange
        String shortCode = "HhqS";
        String originalUrl = "https://github.com/PabloTzeliks";

        ResolveShortCodeRequest request = new ResolveShortCodeRequest(shortCode);

        // 1. Cache Hit
        UrlContext cachedContext = new UrlContext(originalUrl, UUID.randomUUID().toString(), 100);
        when(cache.getUrlContext(shortCode)).thenReturn(Optional.of(cachedContext));
        when(rateLimit.check(anyString(), anyInt())).thenReturn(new RateLimitResult(true, 99, 100));

        // Act
        UrlResponse actualResponse = useCase.execute(request);

        // Assert
        assertNotNull(actualResponse);
        assertEquals(originalUrl, actualResponse.originalUrl());
        verify(cache).getUrlContext(shortCode);
        verify(rateLimit).check(cachedContext.ownerId(), cachedContext.rateLimit());
        verify(repository, never()).findByShortCode(any());
        verify(userRepository, never()).findById(any());
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

        ResolveShortCodeRequest request = new ResolveShortCodeRequest(shortCode);
        UrlResponse expectedResponse = new UrlResponse(originalUrl);

        // 1. Cache Miss
        when(cache.getUrlContext(shortCode)).thenReturn(Optional.empty());

        // 2. PostgreSQL search
        when(repository.findByShortCode(shortCode)).thenReturn(Optional.of(urlFound));

        // 3. Owner lookup (to resolve the plan rate limit)
        User owner = mock(User.class);
        when(owner.getId()).thenReturn(userId);
        when(owner.getPlan()).thenReturn(Plan.FREE);
        when(userRepository.findById(userId)).thenReturn(Optional.of(owner));

        // 4. Rate limit allowed
        when(rateLimit.check(anyString(), anyInt())).thenReturn(new RateLimitResult(true, 99, 100));

        // Act
        UrlResponse actualResponse = useCase.execute(request);

        // Assert
        assertNotNull(actualResponse);
        assertEquals(expectedResponse.originalUrl(), actualResponse.originalUrl());

        verify(cache).getUrlContext(shortCode);
        verify(repository).findByShortCode(shortCode);
        verify(userRepository).findById(userId);
        verify(rateLimit).check(userId.toString(), 100);
    }

    @Test
    @DisplayName("Should correctly implement add Url on Cache after Cache Miss")
    void shouldImplementCachingWhenCacheMiss() {
        String shortCode = "HhqS";
        String originalUrl = "https://github.com/PabloTzeliks";
        LocalDateTime now = LocalDateTime.now();
        UUID userId = UUID.randomUUID();
        Url urlFound = Url.restore(1L, userId, originalUrl, shortCode, now, now.plusDays(7));

        when(cache.getUrlContext(shortCode)).thenReturn(Optional.empty());
        when(repository.findByShortCode(shortCode)).thenReturn(Optional.of(urlFound));

        User owner = mock(User.class);
        when(owner.getId()).thenReturn(userId);
        when(owner.getPlan()).thenReturn(Plan.FREE);
        when(userRepository.findById(userId)).thenReturn(Optional.of(owner));

        when(rateLimit.check(anyString(), anyInt())).thenReturn(new RateLimitResult(true, 99, 100));

        useCase.execute(new ResolveShortCodeRequest(shortCode));

        verify(cache).getUrlContext(shortCode);
        verify(repository).findByShortCode(shortCode);
        verify(cache).put(
                eq("HhqS"),
                eq(new UrlContext(originalUrl, userId.toString(), 100)),
                longThat(ttl -> ttl >= 604795L && ttl <= 604800L)
        );
    }

    @Test
    @DisplayName("Should throw RateLimitExceededException when the owner's limit is exceeded")
    void shouldThrowWhenRateLimitExceeded() {
        String shortCode = "HhqS";
        String originalUrl = "https://github.com/PabloTzeliks";
        UrlContext cachedContext = new UrlContext(originalUrl, UUID.randomUUID().toString(), 100);

        when(cache.getUrlContext(shortCode)).thenReturn(Optional.of(cachedContext));
        when(rateLimit.check(anyString(), anyInt())).thenReturn(new RateLimitResult(false, 0, 100));

        assertThrows(RateLimitExceededException.class,
                () -> useCase.execute(new ResolveShortCodeRequest(shortCode)));
    }

    @Test
    @DisplayName("Should throw UrlExpiredException and not cache expired URL")
    void shouldThrowWhenUrlIsExpired() {
        String shortCode = "HhqS";
        LocalDateTime past = LocalDateTime.now().minusDays(1);
        UUID userId = UUID.randomUUID();
        Url expiredUrl = Url.restore(1L, userId, "https://example.com", shortCode, past.minusDays(7), past);

        when(cache.getUrlContext(shortCode)).thenReturn(Optional.empty());
        when(repository.findByShortCode(shortCode)).thenReturn(Optional.of(expiredUrl));

        assertThrows(UrlExpiredException.class,
                () -> useCase.execute(new ResolveShortCodeRequest(shortCode)));

        verify(userRepository, never()).findById(any());
        verify(rateLimit, never()).check(any(), anyInt());
        verify(cache, never()).put(any(), any(), anyLong());
    }
}
