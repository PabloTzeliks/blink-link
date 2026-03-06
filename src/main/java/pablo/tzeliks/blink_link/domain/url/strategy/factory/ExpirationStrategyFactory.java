package pablo.tzeliks.blink_link.domain.url.strategy.factory;

import pablo.tzeliks.blink_link.domain.url.strategy.ExpirationCalculationStrategy;
import pablo.tzeliks.blink_link.domain.url.strategy.impl.EnterprisePlanExpirationStrategy;
import pablo.tzeliks.blink_link.domain.url.strategy.impl.FreePlanExpirationStrategy;
import pablo.tzeliks.blink_link.domain.url.strategy.impl.VipPlanExpirationStrategy;
import pablo.tzeliks.blink_link.domain.user.model.Plan;

public class ExpirationStrategyFactory {

    public static ExpirationCalculationStrategy getExpirationStrategy(Plan userPlan) {

        return switch (userPlan) {

            case FREE -> new FreePlanExpirationStrategy();
            case VIP -> new VipPlanExpirationStrategy();
            case ENTERPRISE -> new EnterprisePlanExpirationStrategy();
        };
    }
}
