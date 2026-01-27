package pablo.tzeliks.blink_link.infrastructure;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public abstract class AbstractContainerBase {

    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    static {
        postgres.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {

        // URL
        registry.add("spring.datasource.url", postgres::getJdbcUrl);

        // USERNAME
        registry.add("spring.datasource.username", postgres::getUsername);

        // PASSWORD
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}
