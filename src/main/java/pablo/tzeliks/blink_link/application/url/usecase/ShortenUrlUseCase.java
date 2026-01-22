package pablo.tzeliks.blink_link.application.url.usecase;

import org.springframework.transaction.annotation.Transactional;
import pablo.tzeliks.blink_link.application.url.dto.CreateUrlRequest;
import pablo.tzeliks.blink_link.application.url.dto.UrlResponse;
import pablo.tzeliks.blink_link.application.url.mapper.UrlDtoMapper;
import pablo.tzeliks.blink_link.domain.url.model.Url;
import pablo.tzeliks.blink_link.domain.url.ports.ShortenerPort;
import pablo.tzeliks.blink_link.domain.url.ports.UrlRepositoryPort;

public class ShortenUrlUseCase {

    private final ShortenerPort shortener;
    private final UrlRepositoryPort repository;
    private final UrlDtoMapper mapper;

    public ShortenUrlUseCase(ShortenerPort shortener, UrlRepositoryPort repository, UrlDtoMapper mapper) {
        this.shortener = shortener;
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional
    public UrlResponse execute(CreateUrlRequest request) {

        Long id = repository.nextId();
        String shortCode = shortener.encode(id);

        Url url = mapper.toDomain(request, id, shortCode);
        Url savedUrl = repository.save(url);

        return mapper.toDto(savedUrl);
    }
}
