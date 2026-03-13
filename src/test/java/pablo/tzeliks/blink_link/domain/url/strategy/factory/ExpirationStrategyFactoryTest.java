package pablo.tzeliks.blink_link.domain.url.strategy.factory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pablo.tzeliks.blink_link.domain.common.exception.DomainException;
import pablo.tzeliks.blink_link.domain.url.strategy.ExpirationCalculationStrategy;
import pablo.tzeliks.blink_link.domain.url.strategy.impl.EnterprisePlanExpirationStrategy;
import pablo.tzeliks.blink_link.domain.url.strategy.impl.FreePlanExpirationStrategy;
import pablo.tzeliks.blink_link.domain.url.strategy.impl.VipPlanExpirationStrategy;
import pablo.tzeliks.blink_link.domain.user.model.Plan;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ExpirationStrategyFactory}.
 * <p>
 * Validates that the factory returns the correct strategy for each plan
 * and throws the expected exception for null input.
 *
 * @since 3.0.0
 */
class ExpirationStrategyFactoryTest {

    @Test
    @DisplayName("Should return FreePlanExpirationStrategy for FREE plan")
    void shouldReturnFreePlanStrategy() {
        // Act
        ExpirationCalculationStrategy strategy = ExpirationStrategyFactory.getStrategyForPlan(Plan.FREE);

        // Assert
        assertNotNull(strategy);
        assertInstanceOf(FreePlanExpirationStrategy.class, strategy);
    }

    @Test
    @DisplayName("Should return VipPlanExpirationStrategy for VIP plan")
    void shouldReturnVipPlanStrategy() {
        // Act
        ExpirationCalculationStrategy strategy = ExpirationStrategyFactory.getStrategyForPlan(Plan.VIP);

        // Assert
        assertNotNull(strategy);
        assertInstanceOf(VipPlanExpirationStrategy.class, strategy);
    }

    @Test
    @DisplayName("Should return EnterprisePlanExpirationStrategy for ENTERPRISE plan")
    void shouldReturnEnterprisePlanStrategy() {
        // Act
        ExpirationCalculationStrategy strategy = ExpirationStrategyFactory.getStrategyForPlan(Plan.ENTERPRISE);

        // Assert
        assertNotNull(strategy);
        assertInstanceOf(EnterprisePlanExpirationStrategy.class, strategy);
    }

    @Test
    @DisplayName("Should throw DomainException when plan is null")
    void shouldThrowExceptionWhenPlanIsNull() {
        // Act & Assert
        DomainException exception = assertThrows(DomainException.class, () ->
                ExpirationStrategyFactory.getStrategyForPlan(null));

        assertEquals("Authenticated user must have a valid plan.", exception.getMessage());
    }
}
