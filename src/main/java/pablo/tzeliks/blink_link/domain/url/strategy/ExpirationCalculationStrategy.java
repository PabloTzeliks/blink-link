package pablo.tzeliks.blink_link.domain.url.strategy;

import java.time.LocalDateTime;

public interface ExpirationCalculationStrategy {

    LocalDateTime calculateExpirationDate(LocalDateTime now);
}
