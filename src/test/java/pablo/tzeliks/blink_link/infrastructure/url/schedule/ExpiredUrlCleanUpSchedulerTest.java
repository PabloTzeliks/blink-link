package pablo.tzeliks.blink_link.infrastructure.url.schedule;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import pablo.tzeliks.blink_link.application.url.usecase.PurgeUrlsUseCase;
import pablo.tzeliks.blink_link.infrastructure.AbstractContainerBase;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Integration test for {@link ExpiredUrlCleanUpScheduler}.
 * <p>
 * Verifies that the Spring context starts correctly with {@code @EnableScheduling}
 * and that the scheduler bean can be injected along with its UseCase dependency.
 *
 * @since 3.0.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
class ExpiredUrlCleanUpSchedulerTest extends AbstractContainerBase {

    @Autowired
    private ExpiredUrlCleanUpScheduler scheduler;

    @Autowired
    private PurgeUrlsUseCase purgeUrlsUseCase;

    @Test
    @DisplayName("Should load scheduler bean and its UseCase dependency successfully")
    void shouldLoadSchedulerContext() {
        // Assert - both beans are loaded into the context
        assertNotNull(scheduler);
        assertNotNull(purgeUrlsUseCase);
    }

    @Test
    @DisplayName("Should invoke triggerCleanup without errors when called manually")
    void shouldInvokeTriggerCleanupWithoutErrors() {
        // Act & Assert - manual invocation should not throw
        scheduler.triggerCleanup();
    }
}
