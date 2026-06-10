package pablo.tzeliks.blink_link.application.url.port.out;

import java.util.UUID;

public interface RateLimitPort {

    RateLimitResult check(UUID ownerId, int limit);
}
