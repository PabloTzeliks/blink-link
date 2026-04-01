package pablo.tzeliks.blink_link;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for the BlinkLink URL Shortener application.
 * <p>
 * This Spring Boot application provides a professional URL shortening service,
 * allowing users to convert long URLs into compact, shareable short codes.
 * The application follows hexagonal (ports and adapters) architecture principles
 * and is built with enterprise-grade technologies.
 * <p>
 * <b>Technology Stack:</b>
 * <ul>
 *   <li><b>Java 21:</b> Latest LTS version with modern language features</li>
 *   <li><b>Spring Boot 3:</b> Modern Spring framework with Jakarta EE</li>
 *   <li><b>PostgreSQL:</b> Robust relational database for data persistence</li>
 *   <li><b>Flyway:</b> Database migration management</li>
 *   <li><b>Spring Data JPA:</b> Simplified data access layer</li>
 *   <li><b>Hibernate:</b> ORM implementation for JPA</li>
 * </ul>
 * <p>
 * <b>Key Features:</b>
 * <ul>
 *   <li>Base62 encoding for compact, URL-safe short codes</li>
 *   <li>RESTful API with versioning (v2)</li>
 *   <li>Comprehensive error handling with RFC 7807 compliant responses</li>
 *   <li>OpenAPI/Swagger documentation</li>
 *   <li>Transaction management and database optimization</li>
 *   <li>Input validation with Bean Validation</li>
 * </ul>
 * <p>
 * <b>Architecture:</b>
 * <p>
 * The application follows hexagonal architecture with clear separation of concerns:
 * <ul>
 *   <li><b>Domain Layer:</b> Core business logic and models</li>
 *   <li><b>Application Layer:</b> Use cases and orchestration</li>
 *   <li><b>Infrastructure Layer:</b> Controllers, repositories, encoders</li>
 * </ul>
 * <p>
 * <b>Running the Application:</b>
 * <pre>
 * ./mvnw spring-boot:run
 * </pre>
 * Or using Docker:
 * <pre>
 * docker-compose up
 * </pre>
 *
 * @author Pablo Tzeliks
 * @version 3.0.0
 * @since 1.0.0
 */
@SpringBootApplication
@EnableScheduling
@EnableRetry
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}