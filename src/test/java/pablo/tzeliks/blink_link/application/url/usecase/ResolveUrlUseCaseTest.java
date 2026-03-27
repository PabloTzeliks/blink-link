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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Unit tests for the URL resolution use case.
 * <p>
 * This test class validates the {@link ResolveUrlUseCase} business logic in isolation
 * using mocked dependencies. It focuses on testing the use case's behavior without
 * involving databases, controllers, or other infrastructure concerns.
 * <p>
 * <b>Test Strategy:</b>
 * <p>
 * These are pure unit tests that:
 * <ul>
 *   <li>Use Mockito to mock repository and mapper dependencies</li>
 *   <li>Test business logic and validation rules</li>
 *   <li>Verify correct exception handling</li>
 *   <li>Validate interaction with dependencies (method calls, parameters)</li>
 * </ul>
 * <p>
 * <b>Test Coverage:</b>
 * <ul>
 *   <li>Happy path: Successfully resolving an existing short code</li>
 *   <li>Validation: Rejecting empty/null short codes</li>
 *   <li>Not found: Handling non-existent short codes</li>
 * </ul>
 * <p>
 * <b>Benefits of Unit Testing:</b>
 * <ul>
 *   <li>Fast execution (no database or Spring context)</li>
 *   <li>Isolated testing of business logic</li>
 *   <li>Easy to test edge cases and error conditions</li>
 *   <li>Clear verification of component interactions</li>
 * </ul>
 *
 * @author Pablo Tzeliks
 * @version 2.0.0
 * @since 1.0.0
 * @see ResolveUrlUseCase
 */
@ExtendWith(MockitoExtension.class)
class ResolveUrlUseCaseTest {

    @Mock
    private UrlRepositoryPort repository;

    @Mock
    private UrlDtoMapper mapper;

    @InjectMocks
    private ResolveUrlUseCase useCase;

    /**
     * Unit Test: Verifies successful URL resolution from short code.
     * <p>
     * <b>Scenario:</b> Happy Path - Short code exists in system
     * <p>
     * <b>Given:</b> A valid short code "HhqS" that exists in the repository
     * <br><b>When:</b> execute() is called with this short code
     * <br><b>Then:</b> The use case returns a complete UrlResponse with all details
     * <p>
     * <b>Mocked Behavior:</b>
     * <ol>
     *   <li>Repository returns the URL domain object for the given short code</li>
     *   <li>Mapper converts the domain object to a DTO response</li>
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
     *   <li>Repository's findByShortCode() was called once with correct parameter</li>
     *   <li>Mapper's toDto() was called once with the domain object</li>
     * </ul>
     */
    @Test
    @DisplayName("Should return UrlResponse when short code exists")
    void shouldResolveUrlSuccessfully() {
        // Arrange
        String shortCode = "HhqS";
        String originalUrl = "https://github.com/PabloTzeliks";
        LocalDateTime now = LocalDateTime.now();
        UUID userId = UUID.randomUUID();
        Url urlFound = Url.restore(1L, userId, originalUrl, shortCode, now, now.plusDays(7));

        ResolveUrlRequest request = new ResolveUrlRequest(shortCode);
        UrlResponse expectedResponse = new UrlResponse(userId, originalUrl, shortCode, "http://localhost:8080/" + shortCode, now, now.plusDays(7));

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

    /**
     * Unit Test: Verifies validation failure for empty short code.
     * <p>
     * <b>Scenario:</b> Validation Error - Empty short code parameter
     * <p>
     * <b>Given:</b> A request with an empty string as the short code
     * <br><b>When:</b> execute() is called
     * <br><b>Then:</b> InvalidUrlException is thrown before any repository interaction
     * <p>
     * <b>Assertions:</b>
     * <ul>
     *   <li>InvalidUrlException is thrown</li>
     * </ul>
     * <p>
     * <b>Verification:</b>
     * <ul>
     *   <li>Repository is never called (no database interaction for invalid input)</li>
     *   <li>Mapper is never called (validation fails before mapping)</li>
     * </ul>
     * <p>
     * This test validates the fail-fast validation logic that prevents
     * unnecessary database queries for obviously invalid input.
     */
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

    /**
     * Unit Test: Verifies not found exception for non-existent short code.
     * <p>
     * <b>Scenario:</b> Not Found - Short code doesn't exist in database
     * <p>
     * <b>Given:</b> A short code "ghost" that doesn't exist in the repository
     * <br><b>When:</b> execute() is called with this short code
     * <br><b>Then:</b> UrlNotFoundException is thrown with descriptive message
     * <p>
     * <b>Mocked Behavior:</b>
     * <ol>
     *   <li>Repository returns empty Optional for the short code</li>
     * </ol>
     * <p>
     * <b>Assertions:</b>
     * <ul>
     *   <li>UrlNotFoundException is thrown</li>
     * </ul>
     * <p>
     * <b>Verification:</b>
     * <ul>
     *   <li>Repository's findByShortCode() was called with correct parameter</li>
     *   <li>Mapper is never called (no URL to map when not found)</li>
     * </ul>
     * <p>
     * This test validates the exception handling when a user requests a
     * short code that doesn't exist, ensuring proper error messaging for
     * the HTTP 404 response.
     */
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
