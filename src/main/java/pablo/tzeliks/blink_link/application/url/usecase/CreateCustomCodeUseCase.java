package pablo.tzeliks.blink_link.application.url.usecase;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pablo.tzeliks.blink_link.application.url.dto.CreateShortCodeRequest;
import pablo.tzeliks.blink_link.application.url.dto.UrlDetailsResponse;
import pablo.tzeliks.blink_link.application.url.exception.DuplicateCodeException;
import pablo.tzeliks.blink_link.application.url.mapper.UrlDtoMapper;
import pablo.tzeliks.blink_link.application.url.ports.CachePort;
import pablo.tzeliks.blink_link.application.url.ports.SequencePort;
import pablo.tzeliks.blink_link.application.url.validation.CustomCodeValidator;
import pablo.tzeliks.blink_link.application.user.ports.CurrentUserProviderPort;
import pablo.tzeliks.blink_link.domain.url.model.Url;
import pablo.tzeliks.blink_link.domain.url.ports.UrlRepositoryPort;
import pablo.tzeliks.blink_link.domain.url.strategy.ExpirationCalculationStrategy;
import pablo.tzeliks.blink_link.domain.url.strategy.factory.ExpirationStrategyFactory;
import pablo.tzeliks.blink_link.domain.user.exception.InvalidPlanException;
import pablo.tzeliks.blink_link.domain.user.model.Plan;

import java.util.UUID;

@Service
public class CreateCustomCodeUseCase {

    private final UrlRepositoryPort repository;
    private final CurrentUserProviderPort userProvider;
    private final CachePort cache;
    private final CustomCodeValidator validator;
    private final SequencePort sequence;
    private final UrlDtoMapper mapper;

    @Value("${app.cache.max-ttl-seconds:604800}")
    private long maxCacheTtlSeconds;

    public CreateCustomCodeUseCase(UrlRepositoryPort repository,
            CurrentUserProviderPort userProvider,
            CachePort cache,
            CustomCodeValidator validator,
            SequencePort sequence,
            UrlDtoMapper mapper) {

        this.repository = repository;
        this.userProvider = userProvider;
        this.cache = cache;
        this.validator = validator;
        this.sequence = sequence;
        this.mapper = mapper;
    }

    @Transactional
    public UrlDetailsResponse execute(CreateShortCodeRequest request) {

        Plan userPlan = userProvider.getCurrentUserPlan();
        UUID userId = userProvider.getCurrentUserId();

        if (userPlan != Plan.VIP && userPlan != Plan.ENTERPRISE) {
            throw new InvalidPlanException("Custom short codes require VIP or ENTERPRISE plan.");
        }

        String shortCode = request.customCode();

        validator.validate(shortCode);

        if (cache.exists(shortCode)) {
            throw new DuplicateCodeException("Code '" + shortCode + "' is already taken.");
        }

        if (repository.existsByShortCode(shortCode)) {
            throw new DuplicateCodeException("Code '" + shortCode + "' is already taken.");
        }

        ExpirationCalculationStrategy strategy = ExpirationStrategyFactory.getStrategyForPlan(userPlan);

        Long id = sequence.nextId();
        Url url = Url.create(id, userId, request.originalUrl(), shortCode, strategy);

        Url saved = saveUrlToDatabase(url);

        long ttl = Math.min(saved.getSecondsUntilExpiry(), maxCacheTtlSeconds);
        if (ttl > 0) {
            cache.put(shortCode, saved.getOriginalUrl(), ttl);
        }

        return mapper.toDto(saved);
    }

    private Url saveUrlToDatabase(Url url) {
        try {
            return repository.save(url);

        } catch (DataIntegrityViolationException e) {

            throw new DuplicateCodeException("Code '" + url.getShortCode() + "' is already taken.");
        }
    }
}
