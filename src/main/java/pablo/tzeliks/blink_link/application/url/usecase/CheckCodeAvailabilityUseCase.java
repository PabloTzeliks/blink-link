package pablo.tzeliks.blink_link.application.url.usecase;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import pablo.tzeliks.blink_link.application.url.dto.CodeAvailabilityRequest;
import pablo.tzeliks.blink_link.application.url.dto.CodeAvailabilityResponse;
import pablo.tzeliks.blink_link.application.url.ports.CachePort;
import pablo.tzeliks.blink_link.domain.url.exception.InvalidUrlException;
import pablo.tzeliks.blink_link.domain.url.ports.UrlRepositoryPort;

@Service
public class CheckCodeAvailabilityUseCase {

    private final UrlRepositoryPort repository;
    private final CachePort cachePort;

    public CheckCodeAvailabilityUseCase(UrlRepositoryPort repository, CachePort cachePort) {
        this.repository = repository;
        this.cachePort = cachePort;
    }

    public CodeAvailabilityResponse execute(CodeAvailabilityRequest request) {

        String code = request.customCode();

        if (code == null || code.isEmpty()) {
            throw new InvalidUrlException("Custom Code cannot be null or empty");
        }

        if (cachePort.exists(code)) {
            return new CodeAvailabilityResponse(code, false);
        }

        if (repository.existsByShortCode(code)) {
            return new CodeAvailabilityResponse(code, false);
        }

        return new CodeAvailabilityResponse(code, true);
    }
}
