package pablo.tzeliks.blink_link.domain.url.strategy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import pablo.tzeliks.blink_link.domain.url.strategy.impl.EnterprisePlanExpirationStrategy;
import pablo.tzeliks.blink_link.domain.url.strategy.impl.FreePlanExpirationStrategy;
import pablo.tzeliks.blink_link.domain.url.strategy.impl.VipPlanExpirationStrategy;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the expiration calculation strategies.
 * <p>
 * Tests each strategy implementation to ensure the correct expiration date
 * is calculated based on the user's plan.
 *
 * @author QA Test Suite
 * @since 3.0.0
 */
class ExpirationStrategyTest {

    private static final LocalDateTime REFERENCE_TIME = LocalDateTime.of(2026, 1, 1, 12, 0, 0);

    @Nested
    @DisplayName("FreePlanExpirationStrategy Tests")
    class FreePlanTests {

        private final FreePlanExpirationStrategy strategy = new FreePlanExpirationStrategy();

        @Test
        @DisplayName("Should add 7 days to the creation date")
        void shouldAddSevenDays() {
            // Act
            LocalDateTime expirationDate = strategy.calculateExpirationDate(REFERENCE_TIME);

            // Assert
            assertThat(expirationDate).isEqualTo(REFERENCE_TIME.plusDays(7));
        }
    }

    @Nested
    @DisplayName("VipPlanExpirationStrategy Tests")
    class VipPlanTests {

        private final VipPlanExpirationStrategy strategy = new VipPlanExpirationStrategy();

        @Test
        @DisplayName("Should add 1 year to the creation date")
        void shouldAddOneYear() {
            // Act
            LocalDateTime expirationDate = strategy.calculateExpirationDate(REFERENCE_TIME);

            // Assert
            assertThat(expirationDate).isEqualTo(REFERENCE_TIME.plusYears(1));
        }
    }

    @Nested
    @DisplayName("EnterprisePlanExpirationStrategy Tests")
    class EnterprisePlanTests {

        private final EnterprisePlanExpirationStrategy strategy = new EnterprisePlanExpirationStrategy();

        @Test
        @DisplayName("Should add 10 years to the creation date")
        void shouldAddTenYears() {
            // Act
            LocalDateTime expirationDate = strategy.calculateExpirationDate(REFERENCE_TIME);

            // Assert
            assertThat(expirationDate).isEqualTo(REFERENCE_TIME.plusYears(10));
        }
    }
}
