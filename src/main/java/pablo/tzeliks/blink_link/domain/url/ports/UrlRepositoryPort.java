package pablo.tzeliks.blink_link.domain.url.ports;

import pablo.tzeliks.blink_link.domain.url.model.Url;

import java.util.Optional;

public interface UrlRepositoryPort {

    Url save(Url url);

    Url findById(Long id);

    Optional<Url> findByShortCode(String shortCode);
}
