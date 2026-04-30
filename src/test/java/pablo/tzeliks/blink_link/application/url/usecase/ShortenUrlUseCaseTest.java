package pablo.tzeliks.blink_link.application.url.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;
import pablo.tzeliks.blink_link.application.url.dto.CreateShortCodeRequest;
import pablo.tzeliks.blink_link.application.url.dto.UrlDetailsResponse;
import pablo.tzeliks.blink_link.application.url.exception.UrlCollisionException;
import pablo.tzeliks.blink_link.application.url.mapper.UrlDtoMapper;
import pablo.tzeliks.blink_link.application.url.ports.CachePort;
import pablo.tzeliks.blink_link.application.url.ports.SequencePort;
import pablo.tzeliks.blink_link.application.user.ports.CurrentUserProviderPort;
import pablo.tzeliks.blink_link.domain.url.model.Url;
import pablo.tzeliks.blink_link.domain.url.ports.ShortenerPort;
import pablo.tzeliks.blink_link.domain.url.ports.UrlRepositoryPort;
import pablo.tzeliks.blink_link.domain.user.model.Plan;
import pablo.tzeliks.blink_link.infrastructure.url.exception.EncoderException;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the URL shortening use case.
 * <p>
 * This test class validates the {@link ShortenUrlUseCase} orchestration logic in isolation
 * using mocked dependencies. It focuses on testing the complete workflow of URL shortening
 * without involving actual databases or encoders.
 * <p>
 * <b>Test Strategy:</b>
 * <p>
 * These are pure unit tests that:
 * <ul>
 *   <li>Use Mockito to mock all dependencies (shortener, repository, mapper)</li>
 *   <li>Test the orchestration of the complete URL shortening workflow</li>
 *   <li>Verify correct error handling and exception translation</li>
 *   <li>Validate the sequence of operations and method call order</li>
 * </ul>
 * <p>
 * <b>Test Coverage:</b>
 * <ul>
 *   <li>Happy path: Successful URL shortening with all steps in sequence</li>
 *   <li>Error handling: Encoding failure and exception translation</li>
 * </ul>
 * <p>
 * <b>Workflow Under Test:</b>
 * <ol>
 *   <li>Generate unique ID from database sequence</li>
 *   <li>Encode ID to Base62 short code</li>
 *   <li>Map request to domain model</li>
 *   <li>Save domain model to repository</li>
 *   <li>Map saved model to response DTO</li>
 * </ol>
 *
 * @author Pablo Tzeliks
 * @version 2.0.0
 * @since 1.0.0
 * @see ShortenUrlUseCase
 */
@ExtendWith(MockitoExtension.class)
class ShortenUrlUseCaseTest {

    @Mock
    private ShortenerPort shortener;

    @Mock
    private UrlRepositoryPort repository;

    @Mock
    private CachePort cache;

    @Mock
    private UrlDtoMapper mapper;

    @Mock
    private CurrentUserProviderPort userProviderPort;

    @Mock
    private SequencePort sequencePort;

    private ShortenUrlUseCase shortenUrlUseCase;

    private static final long MAX_CACHE_TTL_SECONDS = 604800L;

    @BeforeEach
    void setUp() {
        shortenUrlUseCase = new ShortenUrlUseCase(shortener, repository, userProviderPort, cache, sequencePort, mapper);
        ReflectionTestUtils.setField(shortenUrlUseCase, "maxCacheTtlSeconds", MAX_CACHE_TTL_SECONDS);
    }

    @Test
    @DisplayName("Should orchestrate the URL shortening process correctly: Generate ID -> Get Plan -> Encode to Short Code -> Create Domain -> Save -> Add to Cache -> Map to Response DTO")
    void shouldShortAnUrlCorrectly() {

        // Arrange
        String originalUrl = "https://github.com/PabloTzeliks";
        Long fakeId = 1000000L;
        String fakeShortCode = "HhqS";
        LocalDateTime now = LocalDateTime.now();

        ArgumentCaptor<Long> ttlCaptor = ArgumentCaptor.forClass(Long.class);

        UUID fakeUserId = UUID.randomUUID();

        Url savedUrl = Url.restore(
                fakeId,
                fakeUserId,
                originalUrl,
                fakeShortCode,
                now,
                now.plusDays(7)
        );

        CreateShortCodeRequest request = new CreateShortCodeRequest(originalUrl);

        // 1. SequencePort generate next ID
        when(sequencePort.nextId()).thenReturn(fakeId);

        // 2. Get current user plan
        when(userProviderPort.getCurrentUserPlan()).thenReturn(Plan.FREE);

        // 2b. Get current user ID
        when(userProviderPort.getCurrentUserId()).thenReturn(fakeUserId);

        // 3. Encode ID to Short Code
        when(shortener.encode(fakeId)).thenReturn(fakeShortCode);

        // 4. Save to Repository
        when(repository.save(any(Url.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 5. Map to Response DTO
        UrlDetailsResponse correctResponse = new UrlDetailsResponse(
                fakeUserId,
                originalUrl,
                fakeShortCode,
                "http://localhost:8080/" + fakeShortCode,
                now,
                now.plusDays(7)
        );

        long cachingUrlTtl = Math.min(savedUrl.getSecondsUntilExpiry(), MAX_CACHE_TTL_SECONDS);

        when(mapper.toDto(any(Url.class))).thenReturn(correctResponse);

        // Act
        UrlDetailsResponse trueResponse = shortenUrlUseCase.execute(request);

        // Assert
        assertNotNull(trueResponse);
        assertEquals(correctResponse.originalUrl(), trueResponse.originalUrl());
        assertEquals(correctResponse.shortUrl(), trueResponse.shortUrl());
        assertEquals(correctResponse.shortCode(), trueResponse.shortCode());

        verify(sequencePort).nextId();
        verify(userProviderPort).getCurrentUserPlan();
        verify(userProviderPort).getCurrentUserId();
        verify(shortener).encode(fakeId);
        verify(repository).save(any(Url.class));
        verify(cache).put(eq(fakeShortCode), eq(originalUrl), ttlCaptor.capture());
        Long capturedTtl = ttlCaptor.getValue();
        assertTrue(capturedTtl >= 604790L && capturedTtl <= 604800L,
                "The TTL cache must be approximately 7 days");
        verify(mapper).toDto(any(Url.class));
    }

    /**
     * Unit Test: Verifies exception translation when encoding fails.
     * <p>
     * <b>Scenario:</b> Error Handling - Encoder throws exception
     * <p>
     * <b>Given:</b> An invalid ID (-1) that causes encoding to fail
     * <br><b>When:</b> execute() is called and encoding fails
     * <br><b>Then:</b> EncoderException is caught and re-thrown as InvalidUrlException
     * <p>
     * <b>Mocked Behavior:</b>
     * <ol>
     *   <li>Repository generates an invalid ID (-1)</li>
     *   <li>Shortener throws EncoderException when encoding negative ID</li>
     * </ol>
     * <p>
     * <b>Assertions:</b>
     * <ul>
     *   <li>InvalidUrlException is thrown (not EncoderException)</li>
     *   <li>Exception message is preserved from the original EncoderException</li>
     * </ul>
     * <p>
     * <b>Verification:</b>
     * <ul>
     *   <li>Repository's nextId() was called</li>
     *   <li>Shortener's encode() was called with the invalid ID</li>
     *   <li>Repository's save() was never called (workflow stopped at encoding failure)</li>
     * </ul>
     * <p>
     * This test validates the exception translation strategy where infrastructure
     * exceptions (EncoderException) are wrapped as domain exceptions (InvalidUrlException)
     * to maintain clean separation between layers. This allows the domain/application
     * layer to remain independent of infrastructure-specific exception types.
     */
    /**
     * Unit Test: Verifies exception propagation when encoding fails.
     * <p>
     * <b>Scenario:</b> Error Handling - Encoder throws exception
     * <p>
     * <b>Given:</b> An invalid ID (-1) that causes encoding to fail
     * <br><b>When:</b> execute() is called and encoding fails
     * <br><b>Then:</b> EncoderException propagates from the shortener
     * <p>
     * <b>Note:</b> The current ShortenUrlUseCase does not catch EncoderException
     * and re-throw as InvalidUrlException. The infrastructure exception propagates directly.
     */
    @Test
    @DisplayName("Should throw EncoderException when ShortenerPort implementation fails to encode the ID")
    void shouldThrowEncoderExceptionWhenEncodingFails() {
        // Arrange
        String originalUrl = "https://github.com/PabloTzeliks";
        Long invalidId = -1L;

        CreateShortCodeRequest request = new CreateShortCodeRequest(originalUrl);

        // 1. SequencePort generate next ID (Invalid ID)
        when(sequencePort.nextId()).thenReturn(invalidId);

        // 2. Get current user plan
        when(userProviderPort.getCurrentUserPlan()).thenReturn(Plan.FREE);

        // 2b. Get current user ID
        when(userProviderPort.getCurrentUserId()).thenReturn(UUID.randomUUID());

        // 3. Encode ID to Short Code (will fail)
        when(shortener.encode(invalidId)).thenThrow(new EncoderException("ID cannot be negative"));

        // Act & Assert
        EncoderException exception = assertThrows(EncoderException.class, () -> {
            shortenUrlUseCase.execute(request);
        });

        assertEquals("ID cannot be negative", exception.getMessage());

        // Verify
        verify(sequencePort).nextId();
        verify(userProviderPort).getCurrentUserPlan();
        verify(userProviderPort).getCurrentUserId();
        verify(shortener).encode(invalidId);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Should generate ID via SequencePort")
    void shouldGenerateIdViaSequencePort() {
        // Arrange
        UUID fakeUserId = UUID.randomUUID();
        String originalUrl = "https://github.com/PabloTzeliks";
        CreateShortCodeRequest request = new CreateShortCodeRequest(originalUrl);
        Long generatedId = 1000001L;
        String shortCode = "HhqS1";

        when(sequencePort.nextId()).thenReturn(generatedId);
        when(userProviderPort.getCurrentUserPlan()).thenReturn(Plan.FREE);
        when(userProviderPort.getCurrentUserId()).thenReturn(fakeUserId);
        when(shortener.encode(generatedId)).thenReturn(shortCode);
        when(repository.save(any(Url.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toDto(any(Url.class))).thenReturn(new UrlDetailsResponse(
                fakeUserId,
                originalUrl,
                shortCode,
                "http://localhost:8080/" + shortCode,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(7)
        ));

        // Act
        UrlDetailsResponse response = shortenUrlUseCase.execute(request);

        // Assert
        assertNotNull(response);
        verify(sequencePort, times(1)).nextId();
    }

    @Test
    @DisplayName("Should throw UrlCollisionException on first call and succeed on manual second attempt")
    void shouldRetryOnUrlCollisionException() {
        // Arrange
        UUID fakeUserId = UUID.randomUUID();
        String originalUrl = "https://github.com/PabloTzeliks";
        CreateShortCodeRequest request = new CreateShortCodeRequest(originalUrl);
        String firstCode = "HhqS1";
        String secondCode = "HhqS2";

        when(sequencePort.nextId()).thenReturn(1000001L, 1000002L);
        when(userProviderPort.getCurrentUserPlan()).thenReturn(Plan.FREE);
        when(userProviderPort.getCurrentUserId()).thenReturn(fakeUserId);
        when(shortener.encode(1000001L)).thenReturn(firstCode);
        when(shortener.encode(1000002L)).thenReturn(secondCode);
        when(repository.save(any(Url.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key"))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toDto(any(Url.class))).thenReturn(new UrlDetailsResponse(
                fakeUserId,
                originalUrl,
                secondCode,
                "http://localhost:8080/" + secondCode,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(7)
        ));

        // Act (unit scope: @Retryable proxy is not active, so we simulate a manual second attempt)
        assertThrows(UrlCollisionException.class, () -> shortenUrlUseCase.execute(request));
        UrlDetailsResponse response = shortenUrlUseCase.execute(request);

        // Assert
        assertNotNull(response);
        verify(sequencePort, times(2)).nextId();
        verify(repository, times(2)).save(any(Url.class));
    }

    @Test
    @DisplayName("Should throw propagated exception when collision persists and retries are exceeded")
    void shouldThrowAfterMaxRetriesExceeded() {
        // Arrange
        UUID fakeUserId = UUID.randomUUID();
        CreateShortCodeRequest request = new CreateShortCodeRequest("https://github.com/PabloTzeliks");

        when(sequencePort.nextId()).thenReturn(1000001L);
        when(userProviderPort.getCurrentUserPlan()).thenReturn(Plan.FREE);
        when(userProviderPort.getCurrentUserId()).thenReturn(fakeUserId);
        when(shortener.encode(1000001L)).thenReturn("HhqS1");
        when(repository.save(any(Url.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));

        // Act
        UrlCollisionException exception = assertThrows(UrlCollisionException.class, () -> shortenUrlUseCase.execute(request));

        // Assert
        assertEquals("Colisão no banco de dados", exception.getMessage());
    }
}
