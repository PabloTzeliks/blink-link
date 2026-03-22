package pablo.tzeliks.blink_link.domain.url.strategy.impl;

import pablo.tzeliks.blink_link.domain.url.strategy.ExpirationCalculationStrategy;

import java.time.LocalDateTime;

public class FreePlanExpirationStrategy implements ExpirationCalculationStrategy {

    private static final int DAYS_TO_EXPIRE = 7;

    @Override
    public LocalDateTime calculateExpirationDate(LocalDateTime createdAt) {
        return createdAt.plusDays(DAYS_TO_EXPIRE);
    }
}
