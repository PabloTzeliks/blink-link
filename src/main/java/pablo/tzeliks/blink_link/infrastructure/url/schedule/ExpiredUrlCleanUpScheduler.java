package pablo.tzeliks.blink_link.infrastructure.url.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pablo.tzeliks.blink_link.application.url.usecase.PurgeUrlsUseCase;

@Component
public class ExpiredUrlCleanUpScheduler {

    private static final Logger LOG = LoggerFactory.getLogger(ExpiredUrlCleanUpScheduler.class);
    private final PurgeUrlsUseCase useCase;

    public ExpiredUrlCleanUpScheduler(PurgeUrlsUseCase useCase) {
        this.useCase = useCase;
    }

    @Scheduled(cron = "${app.job.purge-urls.cron}")
    public void triggerCleanup() {
        LOG.info("Starting expired Urls cleanup Schedule.");

        long startDate = System.currentTimeMillis();

        int totalDeleted = useCase.execute();

        long elapsedTime = System.currentTimeMillis() - startDate;

        LOG.info("Schedule terminated. Total Urls removed {} and total elapsed time {}", totalDeleted, elapsedTime);
    }
}
