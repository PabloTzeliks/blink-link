package pablo.tzeliks.blink_link.application.url.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pablo.tzeliks.blink_link.application.url.dto.ResolveUrlRequest;
import pablo.tzeliks.blink_link.application.url.dto.UrlResponse;
import pablo.tzeliks.blink_link.application.url.mapper.UrlDtoMapper;
import pablo.tzeliks.blink_link.domain.url.exception.InvalidUrlException;
import pablo.tzeliks.blink_link.domain.url.exception.UrlNotFoundException;
import pablo.tzeliks.blink_link.domain.url.model.Url;
import pablo.tzeliks.blink_link.domain.url.ports.UrlRepositoryPort;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ResolveUrlUseCaseTest {

    @Mock
    private UrlRepositoryPort repository;

    @Mock
    private UrlDtoMapper mapper;

    @InjectMocks
    private ResolveUrlUseCase useCase;

    @Test
    @DisplayName("Should return UrlResponse when short code exists")
    void shouldResolveUrlSuccessfully() {
        // Arrange
        String shortCode = "HhqS";
        String originalUrl = "https://github.com/PabloTzeliks";
        LocalDateTime now = LocalDateTime.now();
        Url urlFound = new Url(1L, originalUrl, shortCode, now);

        ResolveUrlRequest request = new ResolveUrlRequest(shortCode);
        UrlResponse expectedResponse = new UrlResponse(originalUrl, shortCode, "http://localhost:8080/" + shortCode, now);

        // 1. Repository finds URL by short code
        when(repository.findByShortCode(shortCode)).thenReturn(Optional.of(urlFound));

        // 2. Mapper converts Domain Model to Response DTO
        when(mapper.toDto(urlFound)).thenReturn(expectedResponse);

        // Act
        UrlResponse actualResponse = useCase.execute(request);

        // Assert
        assertNotNull(actualResponse);
        assertEquals(expectedResponse.originalUrl(), actualResponse.originalUrl());
        assertEquals(expectedResponse.shortUrl(), actualResponse.shortUrl());
        assertEquals(expectedResponse.shortCode(), actualResponse.shortCode());

        verify(repository).findByShortCode(shortCode);
        verify(mapper).toDto(urlFound);
    }

    @Test
    @DisplayName("Should throw InvalidUrlException when request is empty")
    void shouldThrowExceptionWhenRequestEmpty() {
        // Arrange
        String blankCode = "";
        ResolveUrlRequest request = new ResolveUrlRequest(blankCode);

        // Act & Assert
        assertThrows(InvalidUrlException.class,
                () -> useCase.execute(request));

        // Verify
        verifyNoInteractions(repository);
        verifyNoInteractions(mapper);
    }

    @Test
    @DisplayName("Should throw UrlNotFoundException when URL not found for the provided short code")
    void shouldThrowExceptionWhenUrlNotFound() {
        // Arrange
        String nonExistentCode = "ghost";
        ResolveUrlRequest request = new ResolveUrlRequest(nonExistentCode);

        // 1. Repository returns empty when searching for non-existent short code
        when(repository.findByShortCode(nonExistentCode)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UrlNotFoundException.class,
                () -> useCase.execute(request));

        // Verify
        verify(repository).findByShortCode(nonExistentCode);

        verifyNoInteractions(mapper);
    }
}
