package pablo.tzeliks.blink_link.application.url.usecase;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pablo.tzeliks.blink_link.application.url.dto.ResolveShortCodeRequest;
import pablo.tzeliks.blink_link.application.url.dto.UrlResponse;
import pablo.tzeliks.blink_link.application.url.ports.CachePort;
import pablo.tzeliks.blink_link.domain.url.exception.InvalidUrlException;
import pablo.tzeliks.blink_link.domain.url.exception.UrlExpiredException;
import pablo.tzeliks.blink_link.domain.url.exception.UrlNotFoundException;
import pablo.tzeliks.blink_link.domain.url.model.Url;
import pablo.tzeliks.blink_link.domain.url.ports.UrlRepositoryPort;

import java.util.Optional;

/**
 *
 * @author Pablo Tzeliks
 * @version 3.0.0
 * @since 1.0.0
 * @see UrlRepositoryPort
 */
@Service
public class RedirectUrlUseCase {

    private final UrlRepositoryPort repository;
    private final CachePort cachePort;

    @Value("${app.cache.max-ttl-seconds:604800}")
    private long maxCacheTtlSeconds;

    public RedirectUrlUseCase(UrlRepositoryPort repository, CachePort cachePort) {
        this.repository = repository;
        this.cachePort = cachePort;
    }

    public UrlResponse execute(ResolveShortCodeRequest request) {

        String shortCode = request.shortCode();

        // Validates URL format
        if (shortCode == null || shortCode.isEmpty()) {
            throw new InvalidUrlException("Short Code cannot be null or empty");
        }

        Optional<String> cachedUrl = cachePort.get(shortCode);

        if (cachedUrl.isPresent()) {

            return new UrlResponse(cachedUrl.get());
        }

        Url urlDb = repository.findByShortCode(shortCode)
                .orElseThrow(() ->
                        new UrlNotFoundException("URL not found for the provided short code: " + shortCode));

        if (urlDb.isExpired()) {
            throw new UrlExpiredException("URL is expired.");
        }

        long domainTtl = urlDb.getSecondsUntilExpiry();
        long finalCacheTtl = Math.min(domainTtl, maxCacheTtlSeconds);

        cachePort.put(shortCode, urlDb.getOriginalUrl(), finalCacheTtl);

        return new UrlResponse(urlDb.getOriginalUrl());
    }
}