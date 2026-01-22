package pablo.tzeliks.blink_link.application.url.usecase;

import pablo.tzeliks.blink_link.application.url.mapper.UrlDtoMapper;
import pablo.tzeliks.blink_link.domain.url.ports.ShortenerPort;
import pablo.tzeliks.blink_link.domain.url.ports.UrlRepositoryPort;

public class ResolveUrlUseCase {

    private final UrlRepositoryPort repository;
    private final UrlDtoMapper mapper;

    public ShortenUrlUseCase(UrlRepositoryPort repository, UrlDtoMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public UrlResponse execute()
}
