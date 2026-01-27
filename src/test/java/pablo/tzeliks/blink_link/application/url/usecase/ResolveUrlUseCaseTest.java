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
import pablo.tzeliks.blink_link.domain.url.model.Url;
import pablo.tzeliks.blink_link.domain.url.ports.UrlRepositoryPort;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
}
