package pablo.tzeliks.blink_link.application.url.usecase;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pablo.tzeliks.blink_link.application.url.dto.ResolveUrlRequest;
import pablo.tzeliks.blink_link.application.url.mapper.UrlDtoMapper;
import pablo.tzeliks.blink_link.domain.url.exception.InvalidUrlException;
import pablo.tzeliks.blink_link.domain.url.ports.ShortenerPort;
import pablo.tzeliks.blink_link.domain.url.ports.UrlRepositoryPort;

@Service
public class ResolveUrlUseCase {

    private final UrlRepositoryPort repository;
    private final UrlDtoMapper mapper;

    @Value("${blink-link.base-url}")
    private String baseUrl;

    public ResolveUrlUseCase(UrlRepositoryPort repository, UrlDtoMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public UrlResponse execute(ResolveUrlRequest request) {

        // Validates URL format
        if (request.shortUrl() == null || request.shortUrl().isEmpty()) {
            throw new InvalidUrlException("Short URL cannot be null or empty");
        }

        if (!request.shortUrl().startsWith(baseUrl)) {
            throw new InvalidUrlException("Invalid short URL format");
        }

        // Extract short code from full short URL
        int index = baseUrl.length() - 1;
        String shortCode = request.shortUrl().substring(index);

        Url urlDb = repository.findByShortCode(shortCode);

        return mapper.toDto(urlDb);
    }
}
