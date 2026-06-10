package pablo.tzeliks.blink_link.application.url.port.out;

public record RateLimitResult(

        boolean isAllowed,
        int remainingRequests,
        int limitApplied
) { }
