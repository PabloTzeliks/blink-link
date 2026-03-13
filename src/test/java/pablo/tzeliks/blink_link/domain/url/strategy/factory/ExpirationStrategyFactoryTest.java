package pablo.tzeliks.blink_link.domain.url.strategy.factory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pablo.tzeliks.blink_link.domain.common.exception.DomainException;
import pablo.tzeliks.blink_link.domain.url.strategy.ExpirationCalculationStrategy;
import pablo.tzeliks.blink_link.domain.url.strategy.impl.EnterprisePlanExpirationStrategy;
import pablo.tzeliks.blink_link.domain.url.strategy.impl.FreePlanExpirationStrategy;
import pablo.tzeliks.blink_link.domain.url.strategy.impl.VipPlanExpirationStrategy;
import pablo.tzeliks.blink_link.domain.user.model.Plan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for the {@link ExpirationStrategyFactory}.
 * <p>
 * Validates that the factory returns the correct strategy for each Plan enum value
 * and throws the expected exception when the plan is null.
 *
 * @author QA Test Suite
 * @since 3.0.0
 */
class ExpirationStrategyFactoryTest {

    @Test
    @DisplayName("Should return FreePlanExpirationStrategy for FREE plan")
    void shouldReturnFreePlanStrategyForFreePlan() {
        // Act
        ExpirationCalculationStrategy strategy = ExpirationStrategyFactory.getStrategyForPlan(Plan.FREE);

        // Assert
        assertThat(strategy).isInstanceOf(FreePlanExpirationStrategy.class);
    }

    @Test
    @DisplayName("Should return VipPlanExpirationStrategy for VIP plan")
    void shouldReturnVipPlanStrategyForVipPlan() {
        // Act
        ExpirationCalculationStrategy strategy = ExpirationStrategyFactory.getStrategyForPlan(Plan.VIP);

        // Assert
        assertThat(strategy).isInstanceOf(VipPlanExpirationStrategy.class);
    }

    @Test
    @DisplayName("Should return EnterprisePlanExpirationStrategy for ENTERPRISE plan")
    void shouldReturnEnterprisePlanStrategyForEnterprisePlan() {
        // Act
        ExpirationCalculationStrategy strategy = ExpirationStrategyFactory.getStrategyForPlan(Plan.ENTERPRISE);

        // Assert
        assertThat(strategy).isInstanceOf(EnterprisePlanExpirationStrategy.class);
    }

    @Test
    @DisplayName("Should throw DomainException when plan is null")
    void shouldThrowDomainExceptionWhenPlanIsNull() {
        // Act & Assert
        assertThatThrownBy(() -> ExpirationStrategyFactory.getStrategyForPlan(null))
                .isInstanceOf(DomainException.class)
                .hasMessage("Authenticated user must have a valid plan.");
    }
}
