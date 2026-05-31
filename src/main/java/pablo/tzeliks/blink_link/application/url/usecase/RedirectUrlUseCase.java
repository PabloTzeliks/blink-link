package pablo.tzeliks.blink_link.application.url.usecase;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pablo.tzeliks.blink_link.application.url.dto.ResolveShortCodeRequest;
import pablo.tzeliks.blink_link.application.url.dto.UrlResponse;
import pablo.tzeliks.blink_link.application.url.exception.OrphanedUrlException;
import pablo.tzeliks.blink_link.application.url.port.out.CachePort;
import pablo.tzeliks.blink_link.application.url.port.out.UrlContext;
import pablo.tzeliks.blink_link.domain.url.exception.InvalidUrlException;
import pablo.tzeliks.blink_link.domain.url.exception.UrlExpiredException;
import pablo.tzeliks.blink_link.domain.url.exception.UrlNotFoundException;
import pablo.tzeliks.blink_link.domain.url.model.Url;
import pablo.tzeliks.blink_link.domain.url.ports.UrlRepositoryPort;
import pablo.tzeliks.blink_link.domain.user.model.User;
import pablo.tzeliks.blink_link.domain.user.policy.PlanRateLimitPolicy;
import pablo.tzeliks.blink_link.domain.user.ports.UserRepositoryPort;

import java.util.Optional;

/**
 *
 * @author Pablo Tzeliks
 * @version 4.0.0
 * @since 1.0.0
 * @see UrlRepositoryPort
 */
@Service
public class RedirectUrlUseCase {

    private final UrlRepositoryPort urlRepository;
    private final UserRepositoryPort userRepository;
    private final CachePort cache;

    @Value("${app.cache.max-ttl-seconds:604800}")
    private long maxCacheTtlSeconds;

    public RedirectUrlUseCase(UrlRepositoryPort urlRepository, UserRepositoryPort userRepository, CachePort cache) {
        this.urlRepository = urlRepository;
        this.userRepository = userRepository;
        this.cache = cache;
    }

    public UrlResponse execute(ResolveShortCodeRequest request) {

        String shortCode = request.shortCode();

        // Validates URL format
        if (shortCode == null || shortCode.isEmpty()) {
            throw new InvalidUrlException("Short Code cannot be null or empty");
        }

        Optional<UrlContext> cachedUrl = cache.getUrlContext(shortCode);

        if (cachedUrl.isPresent()) {

            return new UrlResponse(cachedUrl.get().destination());
        }

        Url urlDb = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() ->
                        new UrlNotFoundException("URL not found for the provided short code: " + shortCode));

        if (urlDb.isExpired()) {
            throw new UrlExpiredException("URL is expired.");
        }

        User ownerUser = userRepository.findById(urlDb.getUserId())
                .orElseThrow(() -> new OrphanedUrlException("Owner User not found for URL with short code: " + shortCode));

        int ownerRateLimit = PlanRateLimitPolicy.requestsPerMinuteForPlan(ownerUser.getPlan());

        UrlContext payload = new UrlContext(urlDb.getOriginalUrl(), ownerUser.getId().toString(), ownerRateLimit);
        long domainTtl = urlDb.getSecondsUntilExpiry();
        long finalCacheTtl = Math.min(domainTtl, maxCacheTtlSeconds);

        if (finalCacheTtl > 0) {
            cache.put(shortCode, payload, finalCacheTtl);
        }

        return new UrlResponse(urlDb.getOriginalUrl());
    }
}