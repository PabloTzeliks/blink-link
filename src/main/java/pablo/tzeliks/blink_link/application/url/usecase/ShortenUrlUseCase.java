package pablo.tzeliks.blink_link.application.url.usecase;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pablo.tzeliks.blink_link.application.url.dto.CreateUrlRequest;
import pablo.tzeliks.blink_link.application.url.dto.UrlDetailsResponse;
import pablo.tzeliks.blink_link.application.url.mapper.UrlDtoMapper;
import pablo.tzeliks.blink_link.application.url.ports.CachePort;
import pablo.tzeliks.blink_link.application.user.ports.CurrentUserProviderPort;
import pablo.tzeliks.blink_link.domain.url.model.Url;
import pablo.tzeliks.blink_link.domain.url.ports.ShortenerPort;
import pablo.tzeliks.blink_link.domain.url.ports.UrlRepositoryPort;
import pablo.tzeliks.blink_link.domain.url.strategy.ExpirationCalculationStrategy;
import pablo.tzeliks.blink_link.domain.url.strategy.factory.ExpirationStrategyFactory;
import pablo.tzeliks.blink_link.domain.user.model.Plan;

import java.util.UUID;

@Service
public class ShortenUrlUseCase {

    private final ShortenerPort shortener;
    private final UrlRepositoryPort repository;
    private final CurrentUserProviderPort userProviderPort;
    private final CachePort cachePort;
    private final UrlDtoMapper mapper;

    @Value("${app.cache.max-ttl-seconds:604800}")
    private long maxCacheTtlSeconds;

    public ShortenUrlUseCase(ShortenerPort shortener,
                             UrlRepositoryPort repository,
                             CurrentUserProviderPort userProviderPort,
                             CachePort cachePort, UrlDtoMapper mapper) {

        this.shortener = shortener;
        this.repository = repository;
        this.userProviderPort = userProviderPort;
        this.cachePort = cachePort;
        this.mapper = mapper;
    }

    @Transactional
    public UrlDetailsResponse execute(CreateUrlRequest request) {

        Long id = repository.nextId();

        Plan userPlan = userProviderPort.getCurrentUserPlan();
        UUID userId = userProviderPort.getCurrentUserId();

        ExpirationCalculationStrategy strategy = ExpirationStrategyFactory.getStrategyForPlan(userPlan);

        String shortCode = shortener.encode(id);

        Url url = Url.create(id, userId, request.originalUrl(), shortCode, strategy);
        Url savedUrl = repository.save(url);

        long domainTtl = savedUrl.getSecondsUntilExpiry();
        long finalCacheTtl = Math.min(domainTtl, maxCacheTtlSeconds);

        if (finalCacheTtl > 0) {
            cachePort.put(shortCode, savedUrl.getOriginalUrl(), finalCacheTtl);
        }

        return mapper.toDto(savedUrl);
    }
}
