package pablo.tzeliks.blink_link.domain.user.model;

import java.time.LocalDateTime;

public enum Plan {
    FREE(7),
    VIP(3650),
    ENTERPRISE(7300);

    private final int expirationDays;

    Plan(int expirationDays) {
        this.expirationDays = expirationDays;
    }

    public LocalDateTime calculateExpiration(LocalDateTime fromDate) {
        return fromDate.plusDays(this.expirationDays);
    }
}
