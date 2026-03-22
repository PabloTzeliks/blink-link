package pablo.tzeliks.blink_link.domain.url.strategy.factory;

import pablo.tzeliks.blink_link.domain.common.exception.DomainException;
import pablo.tzeliks.blink_link.domain.url.strategy.ExpirationCalculationStrategy;
import pablo.tzeliks.blink_link.domain.url.strategy.impl.EnterprisePlanExpirationStrategy;
import pablo.tzeliks.blink_link.domain.url.strategy.impl.FreePlanExpirationStrategy;
import pablo.tzeliks.blink_link.domain.url.strategy.impl.VipPlanExpirationStrategy;
import pablo.tzeliks.blink_link.domain.user.model.Plan;

public class ExpirationStrategyFactory {

    public static ExpirationCalculationStrategy getStrategyForPlan(Plan userPlan) {

        if (userPlan == null) {
            throw new DomainException("Authenticated user must have a valid plan.");
        }

        return switch (userPlan) {

            case FREE -> new FreePlanExpirationStrategy();
            case VIP -> new VipPlanExpirationStrategy();
            case ENTERPRISE -> new EnterprisePlanExpirationStrategy();
        };
    }
}
