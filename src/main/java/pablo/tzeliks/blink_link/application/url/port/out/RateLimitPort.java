package pablo.tzeliks.blink_link.application.url.port.out;

public interface RateLimitPort {

    RateLimitResult check(String ownerId, int limit);
}
