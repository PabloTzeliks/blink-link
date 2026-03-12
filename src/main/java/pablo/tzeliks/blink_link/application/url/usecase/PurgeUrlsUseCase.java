package pablo.tzeliks.blink_link.application.url.usecase;

import org.springframework.stereotype.Service;
import pablo.tzeliks.blink_link.domain.url.ports.UrlRepositoryPort;

@Service
public class PurgeUrlsUseCase {

    private final UrlRepositoryPort repository;
    private final int batchSize;
    private final long sleepTime;

    public PurgeUrlsUseCase(UrlRepositoryPort repository, int batchSize, long sleepTime) {
        this.repository = repository;
        this.batchSize = batchSize;
        this.sleepTime = sleepTime;
    }

    public int execute() {


    }
}
