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