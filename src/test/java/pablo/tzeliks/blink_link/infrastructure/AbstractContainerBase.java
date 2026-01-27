package pablo.tzeliks.blink_link.infrastructure;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Abstract base class for integration tests using Testcontainers.
 * <p>
 * This class provides a shared PostgreSQL container configuration for all integration
 * tests in the application. By extending this class, test classes automatically get
 * access to a real PostgreSQL database running in a Docker container.
 * <p>
 * <b>Testcontainers:</b>
 * <p>
 * Testcontainers is a Java library that provides lightweight, disposable containers
 * for integration testing. It automatically:
 * <ul>
 *   <li>Downloads and starts a PostgreSQL Docker image</li>
 *   <li>Exposes the database on a random available port</li>
 *   <li>Cleans up and stops the container after tests complete</li>
 * </ul>
 * <p>
 * <b>Why Testcontainers?</b>
 * <ul>
 *   <li><b>Real Database:</b> Tests run against actual PostgreSQL, not an in-memory substitute</li>
 *   <li><b>Isolation:</b> Each test run gets a fresh database container</li>
 *   <li><b>Consistency:</b> Same database version as production (PostgreSQL 17)</li>
 *   <li><b>CI/CD Friendly:</b> Works seamlessly in Docker-based CI pipelines</li>
 *   <li><b>No Setup Required:</b> Developers don't need to install PostgreSQL locally</li>
 * </ul>
 * <p>
 * <b>Container Lifecycle:</b>
 * <p>
 * The PostgreSQL container is started once when the class is loaded ({@code static} block)
 * and is shared across all test methods in all subclasses. This improves test performance
 * by avoiding the overhead of starting/stopping containers for each test.
 * <p>
 * <b>Dynamic Configuration:</b>
 * <p>
 * The {@code @DynamicPropertySource} method automatically configures Spring's datasource
 * properties (URL, username, password) to point to the Testcontainers-managed PostgreSQL
 * instance, overriding any properties defined in {@code application.yml}.
 * <p>
 * <b>Usage:</b>
 * <pre>
 * {@code @SpringBootTest}
 * public class MyIntegrationTest extends AbstractContainerBase {
 *     // Test methods have access to PostgreSQL container
 * }
 * </pre>
 *
 * @author Pablo Tzeliks
 * @version 2.0.0
 * @since 1.0.0
 */
@Testcontainers
public abstract class AbstractContainerBase {

    /**
     * Shared PostgreSQL container for all integration tests.
     * <p>
     * Uses PostgreSQL 17 image and is started once per test run.
     * The container is automatically stopped when the JVM exits.
     */
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    static {
        postgres.start();
    }

    /**
     * Configures Spring properties to use the Testcontainers PostgreSQL instance.
     * <p>
     * This method is called by Spring before the test context is created,
     * allowing us to dynamically inject the container's connection details
     * into the Spring configuration.
     * <p>
     * <b>Properties Configured:</b>
     * <ul>
     *   <li>{@code spring.datasource.url} - JDBC URL to the container</li>
     *   <li>{@code spring.datasource.username} - Container's username</li>
     *   <li>{@code spring.datasource.password} - Container's password</li>
     * </ul>
     *
     * @param registry the Spring property registry to add dynamic properties to
     */
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
