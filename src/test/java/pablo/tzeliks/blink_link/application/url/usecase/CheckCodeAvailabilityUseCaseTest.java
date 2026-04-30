package pablo.tzeliks.blink_link.application.url.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.QueryTimeoutException;
import pablo.tzeliks.blink_link.application.url.dto.CodeAvailabilityRequest;
import pablo.tzeliks.blink_link.application.url.dto.CodeAvailabilityResponse;
import pablo.tzeliks.blink_link.application.url.ports.CachePort;
import pablo.tzeliks.blink_link.domain.url.ports.UrlRepositoryPort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckCodeAvailabilityUseCaseTest {

    @Mock
    private UrlRepositoryPort repository;

    @Mock
    private CachePort cachePort;

    private CheckCodeAvailabilityUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CheckCodeAvailabilityUseCase(repository, cachePort);
    }

    @Test
    @DisplayName("Should return available when code is not in Redis and not in the database")
    void shouldReturnAvailable_whenNotInRedisAndNotInDb() {
        when(cachePort.exists("free-code")).thenReturn(false);
        when(repository.existsByShortCode("free-code")).thenReturn(false);

        CodeAvailabilityRequest request = new CodeAvailabilityRequest("free-code");

        CodeAvailabilityResponse response = useCase.execute(request);

        assertThat(response.available()).isTrue();
        assertThat(response.code()).isEqualTo("free-code");
    }

    @Test
    @DisplayName("Should return unavailable when code exists in Redis without querying the database")
    void shouldReturnUnavailable_whenExistsInRedis() {
        when(cachePort.exists("taken-code")).thenReturn(true);

        CodeAvailabilityRequest request = new CodeAvailabilityRequest("taken-code");

        CodeAvailabilityResponse response = useCase.execute(request);

        assertThat(response.available()).isFalse();
        assertThat(response.code()).isEqualTo("taken-code");
        verify(repository, never()).existsByShortCode(any());
    }

    @Test
    @DisplayName("Should return unavailable when Redis misses but the code exists in the database")
    void shouldReturnUnavailable_whenRedisMiss_butExistsInDb() {
        when(cachePort.exists("db-code")).thenReturn(false);
        when(repository.existsByShortCode("db-code")).thenReturn(true);

        CodeAvailabilityRequest request = new CodeAvailabilityRequest("db-code");

        CodeAvailabilityResponse response = useCase.execute(request);

        assertThat(response.available()).isFalse();
        assertThat(response.code()).isEqualTo("db-code");
    }

    @Test
    @DisplayName("Should fall through to database and not propagate the exception when Redis throws DataAccessException")
    void shouldFallThroughToDb_whenRedisThrowsDataAccessException() {
        when(cachePort.exists("fallback-code")).thenThrow(new QueryTimeoutException("Redis timeout"));
        when(repository.existsByShortCode("fallback-code")).thenReturn(false);

        CodeAvailabilityRequest request = new CodeAvailabilityRequest("fallback-code");

        CodeAvailabilityResponse response = useCase.execute(request);

        assertThat(response.available()).isTrue();
        assertThat(response.code()).isEqualTo("fallback-code");
        verify(repository).existsByShortCode("fallback-code");
    }
}
