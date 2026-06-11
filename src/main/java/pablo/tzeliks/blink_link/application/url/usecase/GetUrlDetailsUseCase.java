package pablo.tzeliks.blink_link.application.url.usecase;

import org.springframework.stereotype.Service;
import pablo.tzeliks.blink_link.application.url.dto.ResolveShortCodeRequest;
import pablo.tzeliks.blink_link.application.url.dto.UrlDetailsResponse;
import pablo.tzeliks.blink_link.application.url.mapper.UrlDtoMapper;
import pablo.tzeliks.blink_link.domain.url.exception.InvalidUrlException;
import pablo.tzeliks.blink_link.domain.url.exception.UrlExpiredException;
import pablo.tzeliks.blink_link.domain.url.exception.UrlNotFoundException;
import pablo.tzeliks.blink_link.domain.url.model.Url;
import pablo.tzeliks.blink_link.domain.url.ports.UrlRepositoryPort;

@Service
public class GetUrlDetailsUseCase {

    private final UrlRepositoryPort repository;
    private final UrlDtoMapper mapper;

    public GetUrlDetailsUseCase(UrlRepositoryPort repository, UrlDtoMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public UrlDetailsResponse execute(ResolveShortCodeRequest request) {

        String shortCode = request.shortCode();

        // Validates URL format
        if (shortCode == null || shortCode.isEmpty()) {
            throw new InvalidUrlException("Short Code cannot be null or empty");
        }

        Url urlDb = repository.findByShortCode(shortCode)
                .orElseThrow(() ->
                        new UrlNotFoundException("URL not found for the provided short code: " + shortCode));

        if (urlDb.isExpired()) {
            throw new UrlExpiredException("URL is expired.");
        }

        return mapper.toDto(urlDb);
    }
}
