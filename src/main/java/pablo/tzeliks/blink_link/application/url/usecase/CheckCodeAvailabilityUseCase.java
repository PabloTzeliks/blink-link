package pablo.tzeliks.blink_link.application.url.usecase;

import org.springframework.stereotype.Service;
import pablo.tzeliks.blink_link.application.url.dto.CodeAvailabilityRequest;
import pablo.tzeliks.blink_link.application.url.dto.CodeAvailabilityResponse;
import pablo.tzeliks.blink_link.application.url.ports.CachePort;
import pablo.tzeliks.blink_link.domain.url.exception.InvalidUrlException;
import pablo.tzeliks.blink_link.domain.url.ports.UrlRepositoryPort;

@Service
public class CheckCodeAvailabilityUseCase {

    private final UrlRepositoryPort repository;
    private final CachePort cache;

    public CheckCodeAvailabilityUseCase(UrlRepositoryPort repository, CachePort cache) {
        this.repository = repository;
        this.cache = cache;
    }

    public CodeAvailabilityResponse execute(CodeAvailabilityRequest request) {

        String code = request.customCode();

        if (code == null || code.isEmpty()) {
            throw new InvalidUrlException("Custom Code cannot be null or empty");
        }

        if (cache.exists(code)) {
            return new CodeAvailabilityResponse(code, false);
        }

        if (repository.existsByShortCode(code)) {
            return new CodeAvailabilityResponse(code, false);
        }

        return new CodeAvailabilityResponse(code, true);
    }
}
