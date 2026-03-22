package pablo.tzeliks.blink_link.infrastructure.url.schedule;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pablo.tzeliks.blink_link.application.url.usecase.PurgeUrlsUseCase;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the {@link ExpiredUrlCleanUpScheduler}.
 * <p>
 * Validates that the scheduler correctly invokes the PurgeUrlsUseCase.
 *
 * @author QA Test Suite
 * @since 3.0.0
 */
@ExtendWith(MockitoExtension.class)
class ExpiredUrlCleanUpSchedulerTest {

    @Mock
    private PurgeUrlsUseCase useCase;

    @InjectMocks
    private ExpiredUrlCleanUpScheduler scheduler;

    @Test
    @DisplayName("Should invoke PurgeUrlsUseCase.execute() when triggerCleanup is called")
    void shouldInvokeUseCaseOnTrigger() {
        // Arrange
        when(useCase.execute()).thenReturn(150);

        // Act
        scheduler.triggerCleanup();

        // Assert
        verify(useCase).execute();
    }
}
