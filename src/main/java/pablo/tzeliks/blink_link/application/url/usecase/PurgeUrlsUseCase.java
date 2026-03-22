package pablo.tzeliks.blink_link.application.url.usecase;

import pablo.tzeliks.blink_link.domain.url.ports.UrlRepositoryPort;

import java.time.LocalDateTime;

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

        LocalDateTime now = LocalDateTime.now();
        int totalDeleted = 0;
        int deletedInCurrentBatch;

        do {
            deletedInCurrentBatch = repository.deleteExpiredInBatch(now, batchSize);
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
