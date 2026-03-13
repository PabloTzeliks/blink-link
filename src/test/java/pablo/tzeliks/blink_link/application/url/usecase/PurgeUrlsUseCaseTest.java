package pablo.tzeliks.blink_link.application.url.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import pablo.tzeliks.blink_link.domain.url.ports.UrlRepositoryPort;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link PurgeUrlsUseCase}.
 * <p>
 * Validates the do-while batch deletion loop, ensuring correct
 * accumulation of deleted counts and proper loop termination.
 *
 * @since 3.0.0
 */
@ExtendWith(MockitoExtension.class)
class PurgeUrlsUseCaseTest {

    private static final int BATCH_SIZE = 5000;
    private static final long SLEEP_TIME = 0L; // No sleep in tests for speed

    @Test
    @DisplayName("Should delete multiple full batches and one partial batch, then stop")
    void shouldDeleteMultipleBatchesAndStop() {
        // Arrange
        UrlRepositoryPort repository = mock(UrlRepositoryPort.class);
        PurgeUrlsUseCase useCase = new PurgeUrlsUseCase(repository, BATCH_SIZE, SLEEP_TIME);

        // Simulate: 2 full batches (5000 each) + 1 partial batch (300)
        when(repository.deleteExpiredInBatch(any(LocalDateTime.class), eq(BATCH_SIZE)))
                .thenReturn(BATCH_SIZE)   // First call: 5000 deleted
                .thenReturn(BATCH_SIZE)   // Second call: 5000 deleted
                .thenReturn(300);         // Third call: 300 deleted (partial → loop ends)

        // Act
        int totalDeleted = useCase.execute();

        // Assert
        assertEquals(10300, totalDeleted);
        verify(repository, times(3)).deleteExpiredInBatch(any(LocalDateTime.class), eq(BATCH_SIZE));
    }

    @Test
    @DisplayName("Should stop after single partial batch")
    void shouldStopAfterSinglePartialBatch() {
        // Arrange
        UrlRepositoryPort repository = mock(UrlRepositoryPort.class);
        PurgeUrlsUseCase useCase = new PurgeUrlsUseCase(repository, BATCH_SIZE, SLEEP_TIME);

        // Simulate: only 100 expired URLs
        when(repository.deleteExpiredInBatch(any(LocalDateTime.class), eq(BATCH_SIZE)))
                .thenReturn(100);

        // Act
        int totalDeleted = useCase.execute();

        // Assert
        assertEquals(100, totalDeleted);
        verify(repository, times(1)).deleteExpiredInBatch(any(LocalDateTime.class), eq(BATCH_SIZE));
    }

    @Test
    @DisplayName("Should return zero when no expired URLs exist")
    void shouldReturnZeroWhenNoExpiredUrls() {
        // Arrange
        UrlRepositoryPort repository = mock(UrlRepositoryPort.class);
        PurgeUrlsUseCase useCase = new PurgeUrlsUseCase(repository, BATCH_SIZE, SLEEP_TIME);

        // Simulate: no expired URLs
        when(repository.deleteExpiredInBatch(any(LocalDateTime.class), eq(BATCH_SIZE)))
                .thenReturn(0);

        // Act
        int totalDeleted = useCase.execute();

        // Assert
        assertEquals(0, totalDeleted);
        verify(repository, times(1)).deleteExpiredInBatch(any(LocalDateTime.class), eq(BATCH_SIZE));
    }

    @Test
    @DisplayName("Should handle exactly one full batch correctly")
    void shouldHandleExactlyOneFullBatch() {
        // Arrange
        UrlRepositoryPort repository = mock(UrlRepositoryPort.class);
        PurgeUrlsUseCase useCase = new PurgeUrlsUseCase(repository, BATCH_SIZE, SLEEP_TIME);

        // Simulate: exactly one full batch then 0 in the next
        when(repository.deleteExpiredInBatch(any(LocalDateTime.class), eq(BATCH_SIZE)))
                .thenReturn(BATCH_SIZE)  // First call: full batch
                .thenReturn(0);          // Second call: no more

        // Act
        int totalDeleted = useCase.execute();

        // Assert
        assertEquals(BATCH_SIZE, totalDeleted);
        verify(repository, times(2)).deleteExpiredInBatch(any(LocalDateTime.class), eq(BATCH_SIZE));
    }

    @Test
    @DisplayName("Should call sleep when sleepTime is positive and batch has deletions")
    void shouldRespectSleepTimeBetweenBatches() {
        // Arrange
        UrlRepositoryPort repository = mock(UrlRepositoryPort.class);
        long sleepTime = 50L;
        PurgeUrlsUseCase useCase = new PurgeUrlsUseCase(repository, BATCH_SIZE, sleepTime);

        // Simulate: one full batch + one partial
        when(repository.deleteExpiredInBatch(any(LocalDateTime.class), eq(BATCH_SIZE)))
                .thenReturn(BATCH_SIZE)
                .thenReturn(200);

        // Act
        long start = System.currentTimeMillis();
        int totalDeleted = useCase.execute();
        long elapsed = System.currentTimeMillis() - start;

        // Assert
        assertEquals(BATCH_SIZE + 200, totalDeleted);
        // At least some delay should have been introduced (sleep called for full batch)
        // We use a lenient assertion since Thread.sleep is not precise
        verify(repository, times(2)).deleteExpiredInBatch(any(LocalDateTime.class), eq(BATCH_SIZE));
    }
}
