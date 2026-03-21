package pablo.tzeliks.blink_link.domain.user.model;

import pablo.tzeliks.blink_link.domain.user.exception.InvalidPlanException;

import java.util.Locale;

public enum Plan {

    FREE,
    VIP,
    ENTERPRISE;

    public static Plan fromString(String planName) {
        try {
            return Plan.valueOf(planName.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new InvalidPlanException("Invalid plan.");
        }
    }
}
