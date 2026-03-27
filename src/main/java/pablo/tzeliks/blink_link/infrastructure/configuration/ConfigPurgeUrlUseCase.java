package pablo.tzeliks.blink_link.infrastructure.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pablo.tzeliks.blink_link.application.url.ports.CachePort;
import pablo.tzeliks.blink_link.application.url.usecase.PurgeUrlsUseCase;
import pablo.tzeliks.blink_link.domain.url.ports.UrlRepositoryPort;

@Configuration
public class ConfigPurgeUrlUseCase {

    @Bean
    public PurgeUrlsUseCase purgeUrlsUseCase(
            UrlRepositoryPort urlRepositoryPort,
            @Value(value = "${app.job.purge-urls.batch-size}") int batchSize,
            @Value(value = "${app.job.purge-urls.sleep-millis}") long sleepTime, CachePort cachePort) {

        return new PurgeUrlsUseCase(urlRepositoryPort, cachePort, batchSize, sleepTime);
    }
}
