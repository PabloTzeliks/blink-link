package pablo.tzeliks.blink_link.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for OpenAPI (Swagger) documentation.
 * <p>
 * This configuration class sets up the OpenAPI specification for the BlinkLink API,
 * enabling automatic generation of interactive API documentation through Swagger UI.
 * The documentation is accessible at runtime and provides a user-friendly interface
 * for exploring and testing API endpoints.
 * <p>
 * <b>Why OpenAPI/Swagger?</b> API documentation serves multiple purposes:
 * <ul>
 *   <li><b>Developer Experience:</b> Provides clear, interactive documentation for API consumers</li>
 *   <li><b>Testing:</b> Swagger UI allows developers to test endpoints directly from the browser</li>
 *   <li><b>Standardization:</b> OpenAPI is an industry-standard specification for REST APIs</li>
 *   <li><b>Client Generation:</b> OpenAPI specs can be used to auto-generate client SDKs</li>
 *   <li><b>Contract-First Development:</b> Serves as a contract between frontend and backend teams</li>
 * </ul>
 * <p>
 * The configuration defines basic API metadata including title, description, and version,
 * which appear in the Swagger UI header. Additional configuration can be added to document
 * authentication schemes, servers, tags, and external documentation.
 * <p>
 * <b>Access:</b> When the application is running, the Swagger UI is typically available at
 * {@code http://localhost:8080/swagger-ui.html} and the raw OpenAPI spec at
 * {@code http://localhost:8080/v3/api-docs}.
 *
 * @author Pablo Tzeliks
 * @since 1.0.0
 */
@Configuration
public class OpenApiConfig {

    /**
     * Creates and configures the OpenAPI documentation bean.
     * <p>
     * This bean is picked up by SpringDoc OpenAPI library and used to generate
     * the API documentation. The configuration includes basic API information
     * that describes the purpose and version of the BlinkLink API.
     * <p>
     * The {@link Info} object contains:
     * <ul>
     *   <li><b>Title:</b> The name of the API as displayed in Swagger UI</li>
     *   <li><b>Description:</b> A brief overview of what the API does</li>
     *   <li><b>Version:</b> The current API version for tracking changes</li>
     * </ul>
     *
     * @return a configured {@link OpenAPI} instance with API metadata
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("BlinkLink API 🔗")
                        .description("Professional URLs shortener API developed with Java and Spring Boot.")
                        .version("1.0.0"));
    }
}
}