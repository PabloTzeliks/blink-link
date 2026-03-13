package pablo.tzeliks.blink_link.domain.url.strategy.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for {@link FreePlanExpirationStrategy}.
 * <p>
 * Validates that the FREE plan adds exactly 7 days to the creation date.
 *
 * @since 3.0.0
 */
class FreePlanExpirationStrategyTest {

    private final FreePlanExpirationStrategy strategy = new FreePlanExpirationStrategy();

    @Test
    @DisplayName("Should return expirationDate exactly 7 days after createdAt")
    void shouldAdd7Days() {
        // Arrange
        LocalDateTime createdAt = LocalDateTime.of(2026, 3, 1, 10, 0, 0);

        // Act
        LocalDateTime expirationDate = strategy.calculateExpirationDate(createdAt);

        // Assert
        assertNotNull(expirationDate);
        assertEquals(LocalDateTime.of(2026, 3, 8, 10, 0, 0), expirationDate);
    }

    @Test
    @DisplayName("Should handle month boundary correctly")
    void shouldHandleMonthBoundary() {
        // Arrange
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 28, 0, 0, 0);

        // Act
        LocalDateTime expirationDate = strategy.calculateExpirationDate(createdAt);

        // Assert
        assertEquals(LocalDateTime.of(2026, 2, 4, 0, 0, 0), expirationDate);
    }
}
