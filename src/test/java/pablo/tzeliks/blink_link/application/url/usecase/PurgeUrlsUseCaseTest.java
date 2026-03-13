package pablo.tzeliks.blink_link.application.url.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import pablo.tzeliks.blink_link.domain.url.ports.UrlRepositoryPort;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
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

    private static final int BATCH_SIZE = 5000;
    private static final long SLEEP_MILLIS = 0; // No sleep for test speed

    @Test
    @DisplayName("Should process multiple full batches and one partial batch, accumulating the total count")
    void shouldProcessMultipleFullBatchesAndOnePartialBatch() {
        // Arrange
        UrlRepositoryPort repository = mock(UrlRepositoryPort.class);
        PurgeUrlsUseCase useCase = new PurgeUrlsUseCase(repository, BATCH_SIZE, SLEEP_MILLIS);

        // Simulate: 3 full batches (5000 each) + 1 partial batch (300)
        when(repository.deleteExpiredInBatch(any(LocalDateTime.class), eq(BATCH_SIZE)))
                .thenReturn(BATCH_SIZE)   // 1st batch: 5000
                .thenReturn(BATCH_SIZE)   // 2nd batch: 5000
                .thenReturn(BATCH_SIZE)   // 3rd batch: 5000
                .thenReturn(300);         // 4th batch: 300 (partial → loop ends)

        // Act
        int totalDeleted = useCase.execute();

        // Assert
        assertThat(totalDeleted).isEqualTo(3 * BATCH_SIZE + 300); // 15300
        verify(repository, times(4)).deleteExpiredInBatch(any(LocalDateTime.class), eq(BATCH_SIZE));
    }

    @Test
    @DisplayName("Should terminate immediately when zero records are deleted in the first batch")
    void shouldTerminateWhenZeroRecordsDeleted() {
        // Arrange
        UrlRepositoryPort repository = mock(UrlRepositoryPort.class);
        PurgeUrlsUseCase useCase = new PurgeUrlsUseCase(repository, BATCH_SIZE, SLEEP_MILLIS);

        when(repository.deleteExpiredInBatch(any(LocalDateTime.class), eq(BATCH_SIZE)))
                .thenReturn(0);

        // Act
        int totalDeleted = useCase.execute();

        // Assert
        assertThat(totalDeleted).isZero();
        verify(repository, times(1)).deleteExpiredInBatch(any(LocalDateTime.class), eq(BATCH_SIZE));
    }

    @Test
    @DisplayName("Should process a single partial batch and terminate")
    void shouldProcessSinglePartialBatch() {
        // Arrange
        UrlRepositoryPort repository = mock(UrlRepositoryPort.class);
        PurgeUrlsUseCase useCase = new PurgeUrlsUseCase(repository, BATCH_SIZE, SLEEP_MILLIS);

        when(repository.deleteExpiredInBatch(any(LocalDateTime.class), eq(BATCH_SIZE)))
                .thenReturn(42);

        // Act
        int totalDeleted = useCase.execute();

        // Assert
        assertThat(totalDeleted).isEqualTo(42);
        verify(repository, times(1)).deleteExpiredInBatch(any(LocalDateTime.class), eq(BATCH_SIZE));
    }

    @Test
    @DisplayName("Should process exactly one full batch followed by zero deletions")
    void shouldProcessOneFullBatchThenZero() {
        // Arrange
        UrlRepositoryPort repository = mock(UrlRepositoryPort.class);
        PurgeUrlsUseCase useCase = new PurgeUrlsUseCase(repository, BATCH_SIZE, SLEEP_MILLIS);

        when(repository.deleteExpiredInBatch(any(LocalDateTime.class), eq(BATCH_SIZE)))
                .thenReturn(BATCH_SIZE)   // 1st batch: full
                .thenReturn(0);           // 2nd batch: 0 (partial → loop ends)

        // Act
        int totalDeleted = useCase.execute();

        // Assert
        assertThat(totalDeleted).isEqualTo(BATCH_SIZE);
        verify(repository, times(2)).deleteExpiredInBatch(any(LocalDateTime.class), eq(BATCH_SIZE));
    }
}
