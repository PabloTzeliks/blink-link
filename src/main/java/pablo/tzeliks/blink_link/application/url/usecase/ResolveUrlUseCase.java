package pablo.tzeliks.blink_link.application.url.usecase;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pablo.tzeliks.blink_link.application.url.dto.ResolveUrlRequest;
import pablo.tzeliks.blink_link.application.url.dto.UrlResponse;
import pablo.tzeliks.blink_link.application.url.mapper.UrlDtoMapper;
import pablo.tzeliks.blink_link.domain.url.exception.InvalidUrlException;
import pablo.tzeliks.blink_link.domain.url.exception.UrlNotFoundException;
import pablo.tzeliks.blink_link.domain.url.model.Url;
import pablo.tzeliks.blink_link.domain.url.ports.ShortenerPort;
import pablo.tzeliks.blink_link.domain.url.ports.UrlRepositoryPort;
import pablo.tzeliks.blink_link.infrastructure.exception.EncoderException;

@Service
public class ResolveUrlUseCase {

    private final UrlRepositoryPort repository;
    private final ShortenerPort shortener;
    private final UrlDtoMapper mapper;
    private final String baseUrl;

    public ResolveUrlUseCase(UrlRepositoryPort repository, ShortenerPort shortener, UrlDtoMapper mapper, @Value("${blink-link.base-url}") String baseUrl) {
        this.repository = repository;
        this.shortener = shortener;
        this.mapper = mapper;
        this.baseUrl = baseUrl;
    }

//    @Transactional(readOnly = true)
//    public UrlResponse execute(ResolveUrlRequest request) {
//
//        String inputUrl = request.shortUrl();
//
//        // Validates URL format
//        if (inputUrl == null || inputUrl.isEmpty()) {
//            throw new InvalidUrlException("Short URL cannot be null or empty");
//        }
//
//        String shortCode = extractCode(inputUrl);
//
//        Url urlDb = repository.findByShortCode(shortCode)
//                .orElseThrow(() ->
//                        new UrlNotFoundException("URL not found for the provided short code: " + shortCode));
//
//        return mapper.toDto(urlDb);
//    }
//
//    // Cleans the URL
//    private String extractCode(String inputUrl) {
//
//        String cleanBase = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
//
//        if (!inputUrl.startsWith(cleanBase)) {
//            throw new InvalidUrlException("URL does not start with the expected domain: " + baseUrl);
//        }
//
//        return inputUrl.replace(cleanBase, "");
//    }

    @Transactional(readOnly = true)
    public UrlResponse execute(ResolveUrlRequest request) {
        String shortCode = request.shortUrl();

        if (shortCode == null || shortCode.isEmpty()) {
            throw new InvalidUrlException("Short code cannot be empty");
        }

        Long id;
        try {
            id = shortener.decode(shortCode);
        } catch (EncoderException e) {
            throw new InvalidUrlException("Invalid format: " + shortCode);
        }

        // 2. A Segurança: Buscamos pelo ID no banco
        Url urlDb = repository.findById(id)
                .orElseThrow(() ->
                        new UrlNotFoundException("URL not found for code: " + shortCode)
                );

        return mapper.toDto(urlDb);
    }
}
