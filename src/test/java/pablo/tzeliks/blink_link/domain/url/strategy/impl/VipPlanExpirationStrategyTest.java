package pablo.tzeliks.blink_link.domain.url.strategy.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for {@link VipPlanExpirationStrategy}.
 * <p>
 * Validates that the VIP plan adds exactly 1 year to the creation date.
 *
 * @since 3.0.0
 */
class VipPlanExpirationStrategyTest {

    private final VipPlanExpirationStrategy strategy = new VipPlanExpirationStrategy();

    @Test
    @DisplayName("Should return expirationDate exactly 1 year after createdAt")
    void shouldAdd1Year() {
        // Arrange
        LocalDateTime createdAt = LocalDateTime.of(2026, 3, 1, 10, 0, 0);

        // Act
        LocalDateTime expirationDate = strategy.calculateExpirationDate(createdAt);

        // Assert
        assertNotNull(expirationDate);
        assertEquals(LocalDateTime.of(2027, 3, 1, 10, 0, 0), expirationDate);
    }
}
