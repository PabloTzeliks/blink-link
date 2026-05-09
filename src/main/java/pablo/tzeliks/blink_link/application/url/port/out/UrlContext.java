package pablo.tzeliks.blink_link.application.url.port.out;

public record UrlContext(

        String destination,
        String ownerId,
        int rateLimit
) { }
