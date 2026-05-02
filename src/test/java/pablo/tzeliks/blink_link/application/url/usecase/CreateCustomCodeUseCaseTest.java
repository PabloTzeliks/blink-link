package pablo.tzeliks.blink_link.application.url.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;
import pablo.tzeliks.blink_link.application.url.dto.CreateShortCodeRequest;
import pablo.tzeliks.blink_link.application.url.dto.UrlDetailsResponse;
import pablo.tzeliks.blink_link.application.url.exception.DuplicateCodeException;
import pablo.tzeliks.blink_link.application.url.exception.InvalidCustomCodeException;
import pablo.tzeliks.blink_link.application.url.mapper.UrlDtoMapper;
import pablo.tzeliks.blink_link.application.url.ports.CachePort;
import pablo.tzeliks.blink_link.application.url.ports.SequencePort;
import pablo.tzeliks.blink_link.application.url.validation.CustomCodeValidator;
import pablo.tzeliks.blink_link.application.user.ports.CurrentUserProviderPort;
import pablo.tzeliks.blink_link.domain.url.model.Url;
import pablo.tzeliks.blink_link.domain.url.ports.UrlRepositoryPort;
import pablo.tzeliks.blink_link.domain.user.exception.InvalidPlanException;
import pablo.tzeliks.blink_link.domain.user.model.Plan;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateCustomCodeUseCaseTest {

    @Mock
    private UrlRepositoryPort repository;
    @Mock
    private CurrentUserProviderPort userProvider;
    @Mock
    private CachePort cache;
    @Mock
    private CustomCodeValidator validator;
    @Mock
    private SequencePort sequence;
    @Mock
    private UrlDtoMapper mapper;

    private CreateCustomCodeUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateCustomCodeUseCase(repository, userProvider, cache, validator, sequence, mapper);
        ReflectionTestUtils.setField(useCase, "maxCacheTtlSeconds", 604800L);
    }

    @Test
    @DisplayName("Should create custom code successfully when user is VIP")
    void should_CreateSuccessfully_When_PlanVIP() {
        // Arrange
        UUID userId = UUID.randomUUID();
        CreateShortCodeRequest request = new CreateShortCodeRequest("https://google.com", "mycode");

        when(userProvider.getCurrentUserPlan()).thenReturn(Plan.VIP);
        when(userProvider.getCurrentUserId()).thenReturn(userId);
        when(cache.exists("mycode")).thenReturn(false);
        when(repository.existsByShortCode("mycode")).thenReturn(false);
        doNothing().when(validator).validate("mycode");
        when(sequence.nextId()).thenReturn(1L);
        when(repository.save(any(Url.class))).thenAnswer(inv -> inv.getArgument(0));

        UrlDetailsResponse responseDto = new UrlDetailsResponse(userId, "https://google.com", "mycode",
                "http://localhost/mycode", LocalDateTime.now(), LocalDateTime.now().plusDays(7));
        when(mapper.toDto(any(Url.class))).thenReturn(responseDto);

        // Act
        UrlDetailsResponse response = useCase.execute(request);

        // Assert
        assertNotNull(response);
        verify(validator).validate("mycode");
        verify(sequence).nextId();
        verify(repository).save(any(Url.class));
        verify(cache).put(eq("mycode"), eq("https://google.com"), anyLong());
    }

    @Test
    @DisplayName("Should create custom code successfully when user is ENTERPRISE")
    void should_CreateSuccessfully_When_PlanEnterprise() {
        // Arrange
        UUID userId = UUID.randomUUID();
        CreateShortCodeRequest request = new CreateShortCodeRequest("https://google.com", "mycode");

        when(userProvider.getCurrentUserPlan()).thenReturn(Plan.ENTERPRISE);
        when(userProvider.getCurrentUserId()).thenReturn(userId);
        when(cache.exists("mycode")).thenReturn(false);
        when(repository.existsByShortCode("mycode")).thenReturn(false);
        doNothing().when(validator).validate("mycode");
        when(sequence.nextId()).thenReturn(1L);
        when(repository.save(any(Url.class))).thenAnswer(inv -> inv.getArgument(0));

        UrlDetailsResponse responseDto = new UrlDetailsResponse(userId, "https://google.com", "mycode",
                "http://localhost/mycode", LocalDateTime.now(), LocalDateTime.now().plusDays(7));
        when(mapper.toDto(any(Url.class))).thenReturn(responseDto);

        // Act
        UrlDetailsResponse response = useCase.execute(request);

        // Assert
        assertNotNull(response);
        verify(validator).validate("mycode");
        verify(sequence).nextId();
        verify(repository).save(any(Url.class));
        verify(cache).put(eq("mycode"), eq("https://google.com"), anyLong());
    }

    @Test
    @DisplayName("Should throw InvalidPlanException when user is FREE")
    void should_ThrowException_When_PlanFree() {
        // Arrange
        CreateShortCodeRequest request = new CreateShortCodeRequest("https://google.com", "mycode");
        when(userProvider.getCurrentUserPlan()).thenReturn(Plan.FREE);
        when(userProvider.getCurrentUserId()).thenReturn(UUID.randomUUID());

        // Act & Assert
        assertThrows(InvalidPlanException.class, () -> useCase.execute(request));

        verify(validator, never()).validate(any());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Should propagate InvalidCustomCodeException when validator fails")
    void should_PropagateException_When_ValidatorFails() {
        // Arrange
        UUID userId = UUID.randomUUID();
        CreateShortCodeRequest request = new CreateShortCodeRequest("https://google.com", "invalid-");

        when(userProvider.getCurrentUserPlan()).thenReturn(Plan.VIP);
        when(userProvider.getCurrentUserId()).thenReturn(userId);
        when(cache.exists("invalid-")).thenReturn(false);
        when(repository.existsByShortCode("invalid-")).thenReturn(false);
        doThrow(new InvalidCustomCodeException("Format error")).when(validator).validate("invalid-");

        // Act & Assert
        assertThrows(InvalidCustomCodeException.class, () -> useCase.execute(request));

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw DuplicateCodeException on database unique constraint violation")
    void should_ThrowDuplicateCodeException_When_UniqueViolation() {
        // Arrange
        UUID userId = UUID.randomUUID();
        CreateShortCodeRequest request = new CreateShortCodeRequest("https://google.com", "mycode");

        when(userProvider.getCurrentUserPlan()).thenReturn(Plan.VIP);
        when(userProvider.getCurrentUserId()).thenReturn(userId);
        when(cache.exists("mycode")).thenReturn(false);
        when(repository.existsByShortCode("mycode")).thenReturn(false);
        when(sequence.nextId()).thenReturn(1L);
        when(repository.save(any(Url.class))).thenThrow(new DataIntegrityViolationException("Unique index violation"));

        // Act & Assert
        assertThrows(DuplicateCodeException.class, () -> useCase.execute(request));
    }

    @Test
    @DisplayName("Should degrade silently and return successfully when Redis throws exception on cache put")
    void should_DegradeSilently_When_CachePutFails() {
        // Arrange
        UUID userId = UUID.randomUUID();
        CreateShortCodeRequest request = new CreateShortCodeRequest("https://google.com", "mycode");

        when(userProvider.getCurrentUserPlan()).thenReturn(Plan.VIP);
        when(userProvider.getCurrentUserId()).thenReturn(userId);
        when(cache.exists("mycode")).thenReturn(false);
        when(repository.existsByShortCode("mycode")).thenReturn(false);
        when(sequence.nextId()).thenReturn(1L);
        when(repository.save(any(Url.class))).thenAnswer(inv -> inv.getArgument(0));

        doThrow(new DataAccessException("Redis down") {
        }).when(cache).put(anyString(), anyString(), anyLong());

        UrlDetailsResponse responseDto = new UrlDetailsResponse(userId, "https://google.com", "mycode",
                "http://localhost/mycode", LocalDateTime.now(), LocalDateTime.now().plusDays(7));
        when(mapper.toDto(any(Url.class))).thenReturn(responseDto);

        // Act
        UrlDetailsResponse response = useCase.execute(request);

        // Assert
        assertNotNull(response);
    }
}
