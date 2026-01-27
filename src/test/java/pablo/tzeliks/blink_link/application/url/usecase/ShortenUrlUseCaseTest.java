package pablo.tzeliks.blink_link.application.url.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pablo.tzeliks.blink_link.application.url.dto.CreateUrlRequest;
import pablo.tzeliks.blink_link.application.url.dto.UrlResponse;
import pablo.tzeliks.blink_link.application.url.mapper.UrlDtoMapper;
import pablo.tzeliks.blink_link.domain.url.exception.InvalidUrlException;
import pablo.tzeliks.blink_link.domain.url.model.Url;
import pablo.tzeliks.blink_link.domain.url.ports.ShortenerPort;
import pablo.tzeliks.blink_link.domain.url.ports.UrlRepositoryPort;
import pablo.tzeliks.blink_link.infrastructure.exception.EncoderException;

import java.time.LocalDateTime;

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
    private UrlDtoMapper mapper;

    @InjectMocks
    private ShortenUrlUseCase shortenUrlUseCase;

    /**
     * Unit Test: Verifies complete URL shortening workflow orchestration.
     * <p>
     * <b>Scenario:</b> Happy Path - All components work together correctly
     * <p>
     * <b>Given:</b> A valid long URL to be shortened
     * <br><b>When:</b> execute() is called
     * <br><b>Then:</b> The use case orchestrates all steps and returns a complete response
     * <p>
     * <b>Workflow Steps Tested:</b>
     * <ol>
     *   <li>Repository generates next ID (1000000)</li>
     *   <li>Shortener encodes ID to Base62 ("HhqS")</li>
     *   <li>Mapper creates domain model from request + ID + short code</li>
     *   <li>Repository saves the domain model</li>
     *   <li>Mapper converts saved model to response DTO</li>
     * </ol>
     * <p>
     * <b>Assertions:</b>
     * <ul>
     *   <li>Response is not null</li>
     *   <li>Response contains correct original URL</li>
     *   <li>Response contains correct short URL</li>
     *   <li>Response contains correct short code</li>
     * </ul>
     * <p>
     * <b>Verification:</b>
     * <ul>
     *   <li>All mocked methods were called exactly once in the correct order</li>
     *   <li>Each method received the expected parameters</li>
     * </ul>
     * <p>
     * This test validates the use case's role as an orchestrator, ensuring it
     * correctly coordinates between the encoder, repository, and mapper layers.
     */
    @Test
    @DisplayName("Should orchestrate the URL shortening process correctly: Generate ID -> Encode to Short Code -> Map to Domain -> Save -> Map to Response DTO")
    void shouldShortAnUrlCorrectly() {
        // Arrange
        String originalUrl = "https://github.com/PabloTzeliks";
        Long fakeId = 1000000L;
        String fakeShortCode = "HhqS";
        LocalDateTime now = LocalDateTime.now();

        CreateUrlRequest request = new CreateUrlRequest(originalUrl);

        // 1. Repository generate next ID
        when(repository.nextId()).thenReturn(fakeId);

        // 2. Encode ID to Short Code
        when(shortener.encode(fakeId)).thenReturn(fakeShortCode);

        // 3. Map to Domain Model
        Url url = new Url(fakeId, originalUrl, fakeShortCode, now);

        when(mapper.toDomain(request, fakeId, fakeShortCode))
                .thenReturn(url);

        // 4. Save to Repository
        when(repository.save(any(Url.class))).thenReturn(url);

        // 5. Map to Response DTO
        UrlResponse correctResponse = new UrlResponse(
                originalUrl,
                fakeShortCode,
                "http://localhost:8080/" + fakeShortCode,
                now
        );

        when(mapper.toDto(url)).thenReturn(correctResponse);

        // Act
        UrlResponse trueResponse = shortenUrlUseCase.execute(request);

        // Assert
        assertNotNull(trueResponse);
        assertEquals(correctResponse.originalUrl(), trueResponse.originalUrl());
        assertEquals(correctResponse.shortUrl(), trueResponse.shortUrl());
        assertEquals(correctResponse.shortCode(), trueResponse.shortCode());

        verify(repository).nextId();
        verify(shortener).encode(fakeId);
        verify(mapper).toDomain(request, fakeId, fakeShortCode);
        verify(repository).save(url);
        verify(mapper).toDto(url);
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
    @Test
    @DisplayName("Should throw InvalidUrlException when ShortenerPort implementation fails to encode the ID")
    void shouldThrowInvalidUrlExceptionWhenEncodingFails() {
        // Arrange
        String originalUrl = "https://github.com/PabloTzeliks";
        Long invalidId = -1L;

        CreateUrlRequest request = new CreateUrlRequest(originalUrl);

        // 1. Repository generate next ID (Invalid ID)
        when(repository.nextId()).thenReturn(invalidId);

        // 2. Encode ID to Short Code (will fail)
        when(shortener.encode(invalidId)).thenThrow(new EncoderException("ID cannot be negative"));

        // Act & Assert
        InvalidUrlException exception = assertThrows(InvalidUrlException.class, () -> {
            shortenUrlUseCase.execute(request);
        });

        assertEquals("ID cannot be negative", exception.getMessage());

        // Verify
        verify(repository).nextId();
        verify(shortener).encode(invalidId);

        verify(repository, never()).save(any());
    }
}