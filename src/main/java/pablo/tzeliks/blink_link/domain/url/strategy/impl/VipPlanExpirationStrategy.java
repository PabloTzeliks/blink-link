package pablo.tzeliks.blink_link.domain.url.strategy.impl;

import pablo.tzeliks.blink_link.domain.url.strategy.ExpirationCalculationStrategy;

import java.time.LocalDateTime;

public class VipPlanExpirationStrategy implements ExpirationCalculationStrategy {

    private static final int YEARS_TO_EXPIRE = 1;

    @Override
    public LocalDateTime calculateExpirationDate(LocalDateTime createdAt) {
        return createdAt.plusYears(YEARS_TO_EXPIRE);
    }
}
