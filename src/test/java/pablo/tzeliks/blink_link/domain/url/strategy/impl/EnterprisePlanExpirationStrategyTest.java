package pablo.tzeliks.blink_link.domain.url.strategy.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for {@link EnterprisePlanExpirationStrategy}.
 * <p>
 * Validates that the ENTERPRISE plan adds exactly 10 years to the creation date.
 *
 * @since 3.0.0
 */
class EnterprisePlanExpirationStrategyTest {

    private final EnterprisePlanExpirationStrategy strategy = new EnterprisePlanExpirationStrategy();

    @Test
    @DisplayName("Should return expirationDate exactly 10 years after createdAt")
    void shouldAdd10Years() {
        // Arrange
        LocalDateTime createdAt = LocalDateTime.of(2026, 3, 1, 10, 0, 0);

        // Act
        LocalDateTime expirationDate = strategy.calculateExpirationDate(createdAt);

        // Assert
        assertNotNull(expirationDate);
        assertEquals(LocalDateTime.of(2036, 3, 1, 10, 0, 0), expirationDate);
    }
}
