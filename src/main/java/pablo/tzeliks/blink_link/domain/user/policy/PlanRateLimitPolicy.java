package pablo.tzeliks.blink_link.domain.user.policy;

import pablo.tzeliks.blink_link.domain.common.exception.DomainException;
import pablo.tzeliks.blink_link.domain.user.model.Plan;

public final class PlanRateLimitPolicy {

    public static int requestsPerMinuteForPlan(Plan plan) {

        if (plan == null) {
            throw new DomainException("Authenticated user must have a valid plan.");
        }

        return switch (plan) {
            case FREE -> 100;
            case VIP -> 500;
            case ENTERPRISE -> 2000;
        };
    }
}
