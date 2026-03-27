package pablo.tzeliks.blink_link.application.url.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pablo.tzeliks.blink_link.application.url.dto.CreateUrlRequest;
import pablo.tzeliks.blink_link.application.url.dto.UrlResponse;
import pablo.tzeliks.blink_link.application.url.mapper.UrlDtoMapper;
import pablo.tzeliks.blink_link.domain.url.model.Url;
import pablo.tzeliks.blink_link.application.user.ports.CurrentUserProviderPort;
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
    private final UrlDtoMapper mapper;
    private final CurrentUserProviderPort userProviderPort;

    public ShortenUrlUseCase(ShortenerPort shortener, UrlRepositoryPort repository, UrlDtoMapper mapper, CurrentUserProviderPort userProviderPort) {
        this.shortener = shortener;
        this.repository = repository;
        this.mapper = mapper;
        this.userProviderPort = userProviderPort;
    }

    @Transactional
    public UrlResponse execute(CreateUrlRequest request) {

        Long id = repository.nextId();

        Plan userPlan = userProviderPort.getCurrentUserPlan();
        UUID userId = userProviderPort.getCurrentUserId();

        ExpirationCalculationStrategy strategy = ExpirationStrategyFactory.getStrategyForPlan(userPlan);

        String shortCode = shortener.encode(id);

        Url url = Url.create(id, userId, request.originalUrl(), shortCode, strategy);
        Url savedUrl = repository.save(url);

        return mapper.toDto(savedUrl);
    }
}
