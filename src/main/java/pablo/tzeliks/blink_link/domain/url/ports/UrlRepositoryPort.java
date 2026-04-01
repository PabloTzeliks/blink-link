package pablo.tzeliks.blink_link.domain.url.ports;

import pablo.tzeliks.blink_link.domain.url.model.Url;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author Pablo Tzeliks
 * @version 2.0.0
 * @since 1.0.0
 */
public interface UrlRepositoryPort {

    Url save(Url url);

    Optional<Url> findById(Long id);

    Optional<Url> findByShortCode(String shortCode);

    List<String> deleteExpiredInBatchReturningCodes(LocalDateTime referenceTime, int batchSize);
}
