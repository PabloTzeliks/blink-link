package pablo.tzeliks.blink_link.application.url.usecase;

import pablo.tzeliks.blink_link.application.url.ports.CachePort;
import pablo.tzeliks.blink_link.domain.url.ports.UrlRepositoryPort;

import java.time.LocalDateTime;
import java.util.List;

public class PurgeUrlsUseCase {

    private final UrlRepositoryPort repository;
    private final CachePort cachePort;
    private final int batchSize;
    private final long sleepTime;

    public PurgeUrlsUseCase(UrlRepositoryPort repository,
                            CachePort cachePort,
                            int batchSize,
                            long sleepTime) {

        this.repository = repository;
        this.cachePort = cachePort;
        this.batchSize = batchSize;
        this.sleepTime = sleepTime;
    }

    public int execute() {

        LocalDateTime now = LocalDateTime.now();
        int totalDeleted = 0;
        int deletedInCurrentBatch;

        do {
            List<String> deletedCodes = repository.deleteExpiredInBatchReturningCodes(now, batchSize);
            deletedCodes.forEach(cachePort::evict);

            deletedInCurrentBatch = deletedCodes.size();

            totalDeleted += deletedInCurrentBatch;

            if (deletedInCurrentBatch > 0 && sleepTime > 0) {
                sleepForDatabaseRelief();
            }

        } while (deletedInCurrentBatch == batchSize);

        return totalDeleted;
    }

    private void sleepForDatabaseRelief() {
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
