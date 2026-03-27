package pablo.tzeliks.blink_link.application.url.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pablo.tzeliks.blink_link.application.url.ports.CachePort;
import pablo.tzeliks.blink_link.domain.url.ports.UrlRepositoryPort;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the {@link PurgeUrlsUseCase}.
 * <p>
 * Tests the do-while batch deletion loop logic, verifying that:
 * - Multiple full batches are processed correctly
 * - A partial final batch terminates the loop
 * - Zero deletions terminate immediately
 * - Total count is accumulated correctly across batches
 *
 * @author QA Test Suite
 * @since 3.0.0
 */
@ExtendWith(MockitoExtension.class)
class PurgeUrlsUseCaseTest {

    private static final int BATCH_SIZE = 3;
    private static final long SLEEP_MILLIS = 0; // No sleep for test speed

    @Mock
    private UrlRepositoryPort repository;

    @Mock
    private CachePort cachePort;

    @Test
    @DisplayName("Should process multiple full batches and one partial batch, accumulating the total count")
    void shouldProcessMultipleFullBatchesAndOnePartialBatch() {
        // Arrange
        PurgeUrlsUseCase useCase = new PurgeUrlsUseCase(repository, cachePort, BATCH_SIZE, SLEEP_MILLIS);

        // Simulate: 3 full batches + 1 partial batch
        when(repository.deleteExpiredInBatchReturningCodes(any(LocalDateTime.class), eq(BATCH_SIZE)))
                .thenReturn(List.of("c1", "c2", "c3"))   // 1st batch: full
                .thenReturn(List.of("c4", "c5", "c6"))   // 2nd batch: full
                .thenReturn(List.of("c7", "c8", "c9"))   // 3rd batch: full
                .thenReturn(List.of("c10"));               // 4th batch: partial → loop ends

        // Act
        useCase.execute();

        // Assert
        verify(repository, times(4)).deleteExpiredInBatchReturningCodes(any(LocalDateTime.class), eq(BATCH_SIZE));
        verify(cachePort).evict("c1");
        verify(cachePort).evict("c2");
        verify(cachePort).evict("c3");
        verify(cachePort).evict("c4");
        verify(cachePort).evict("c5");
        verify(cachePort).evict("c6");
        verify(cachePort).evict("c7");
        verify(cachePort).evict("c8");
        verify(cachePort).evict("c9");
        verify(cachePort).evict("c10");
    }

    @Test
    @DisplayName("Should terminate immediately when zero records are deleted in the first batch")
    void shouldTerminateWhenZeroRecordsDeleted() {
        // Arrange
        PurgeUrlsUseCase useCase = new PurgeUrlsUseCase(repository, cachePort, BATCH_SIZE, SLEEP_MILLIS);

        when(repository.deleteExpiredInBatchReturningCodes(any(LocalDateTime.class), eq(BATCH_SIZE)))
                .thenReturn(List.of());

        // Act
        useCase.execute();

        // Assert
        verify(repository, times(1)).deleteExpiredInBatchReturningCodes(any(LocalDateTime.class), eq(BATCH_SIZE));
        verifyNoInteractions(cachePort);
    }

    @Test
    @DisplayName("Should process a single partial batch and terminate")
    void shouldProcessSinglePartialBatch() {
        // Arrange
        PurgeUrlsUseCase useCase = new PurgeUrlsUseCase(repository, cachePort, BATCH_SIZE, SLEEP_MILLIS);

        when(repository.deleteExpiredInBatchReturningCodes(any(LocalDateTime.class), eq(BATCH_SIZE)))
                .thenReturn(List.of("a1", "a2"));

        // Act
        useCase.execute();

        // Assert
        verify(repository, times(1)).deleteExpiredInBatchReturningCodes(any(LocalDateTime.class), eq(BATCH_SIZE));
        verify(cachePort).evict("a1");
        verify(cachePort).evict("a2");
    }

    @Test
    @DisplayName("Should process exactly one full batch followed by zero deletions")
    void shouldProcessOneFullBatchThenZero() {
        // Arrange
        PurgeUrlsUseCase useCase = new PurgeUrlsUseCase(repository, cachePort, BATCH_SIZE, SLEEP_MILLIS);

        when(repository.deleteExpiredInBatchReturningCodes(any(LocalDateTime.class), eq(BATCH_SIZE)))
                .thenReturn(List.of("b1", "b2", "b3"))   // 1st batch: full
                .thenReturn(List.of());                    // 2nd batch: 0 (partial → loop ends)

        // Act
        useCase.execute();

        // Assert
        verify(repository, times(2)).deleteExpiredInBatchReturningCodes(any(LocalDateTime.class), eq(BATCH_SIZE));
        verify(cachePort).evict("b1");
        verify(cachePort).evict("b2");
        verify(cachePort).evict("b3");
    }
}
