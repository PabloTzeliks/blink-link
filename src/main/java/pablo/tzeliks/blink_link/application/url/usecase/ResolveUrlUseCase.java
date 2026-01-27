package pablo.tzeliks.blink_link.application.url.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pablo.tzeliks.blink_link.application.url.dto.ResolveUrlRequest;
import pablo.tzeliks.blink_link.application.url.dto.UrlResponse;
import pablo.tzeliks.blink_link.application.url.mapper.UrlDtoMapper;
import pablo.tzeliks.blink_link.domain.url.exception.InvalidUrlException;
import pablo.tzeliks.blink_link.domain.url.exception.UrlNotFoundException;
import pablo.tzeliks.blink_link.domain.url.model.Url;
import pablo.tzeliks.blink_link.domain.url.ports.UrlRepositoryPort;

@Service
public class ResolveUrlUseCase {

    private final UrlRepositoryPort repository;
    private final UrlDtoMapper mapper;

    public ResolveUrlUseCase(UrlRepositoryPort repository, UrlDtoMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public UrlResponse execute(ResolveUrlRequest request) {

        String shortCode = request.shortUrl();

        // Validates URL format
        if (shortCode == null || shortCode.isEmpty()) {
            throw new InvalidUrlException("Short Code cannot be null or empty");
        }

        Url urlDb = repository.findByShortCode(shortCode)
                .orElseThrow(() ->
                        new UrlNotFoundException("URL not found for the provided short code: " + shortCode));

        return mapper.toDto(urlDb);
    }
}