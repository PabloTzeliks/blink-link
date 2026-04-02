package pablo.tzeliks.blink_link.application.url.usecase;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pablo.tzeliks.blink_link.application.url.dto.CreateUrlRequest;
import pablo.tzeliks.blink_link.application.url.dto.UrlDetailsResponse;
import pablo.tzeliks.blink_link.application.url.exception.UrlCollisionException;
import pablo.tzeliks.blink_link.application.url.mapper.UrlDtoMapper;
import pablo.tzeliks.blink_link.application.url.ports.CachePort;
import pablo.tzeliks.blink_link.application.url.ports.SequencePort;
import pablo.tzeliks.blink_link.application.user.ports.CurrentUserProviderPort;
import pablo.tzeliks.blink_link.domain.url.model.Url;
import pablo.tzeliks.blink_link.domain.url.ports.ShortenerPort;
import pablo.tzeliks.blink_link.domain.url.ports.UrlRepositoryPort;
import pablo.tzeliks.blink_link.domain.url.strategy.ExpirationCalculationStrategy;
import pablo.tzeliks.blink_link.domain.url.strategy.factory.ExpirationStrategyFactory;
import pablo.tzeliks.blink_link.domain.user.model.Plan;

import java.util.UUID;

@Slf4j
@Service
public class ShortenUrlUseCase {

    private final ShortenerPort shortener;
    private final UrlRepositoryPort repository;
    private final CurrentUserProviderPort userProviderPort;
    private final CachePort cachePort;
    private final SequencePort sequencePort;
    private final UrlDtoMapper mapper;

    @Value("${app.cache.max-ttl-seconds:604800}")
    private long maxCacheTtlSeconds;

    public ShortenUrlUseCase(ShortenerPort shortener,
                             UrlRepositoryPort repository,
                             CurrentUserProviderPort userProviderPort,
                             CachePort cachePort, SequencePort sequencePort,
                             UrlDtoMapper mapper) {

        this.shortener = shortener;
        this.repository = repository;
        this.userProviderPort = userProviderPort;
        this.cachePort = cachePort;
        this.sequencePort = sequencePort;
        this.mapper = mapper;
    }

    @Transactional
    @Retryable(
            retryFor = { UrlCollisionException.class },
            maxAttemptsExpression = "${app.url.creation.max-retries:3}",
            backoff = @Backoff(delay = 100)
    )
    public UrlDetailsResponse execute(CreateUrlRequest request) {

        Plan userPlan = userProviderPort.getCurrentUserPlan();
        UUID userId = userProviderPort.getCurrentUserId();
        ExpirationCalculationStrategy strategy = ExpirationStrategyFactory.getStrategyForPlan(userPlan);

        Long id = sequencePort.nextId();
        String shortCode = shortener.encode(id);
        Url url = Url.create(id, userId, request.originalUrl(), shortCode, strategy);

        Url savedUrl = saveUrlToDatabase(url);

        long finalCacheTtl = Math.min(savedUrl.getSecondsUntilExpiry(), maxCacheTtlSeconds);
        if (finalCacheTtl > 0) {
            cachePort.put(shortCode, savedUrl.getOriginalUrl(), finalCacheTtl);
        }

        return mapper.toDto(savedUrl);
    }

    private Url saveUrlToDatabase(Url url) {

        try {
            return repository.save(url);

        } catch (DataIntegrityViolationException e) {
            log.warn("Collision detected on shortCode {}. Spring will make the Retry.", url.getShortCode());

            throw new UrlCollisionException("Colisão no banco de dados", e);
        }
    }
}
