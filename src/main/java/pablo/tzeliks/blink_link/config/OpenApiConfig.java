package pablo.tzeliks.blink_link.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("BlinkLink API 🔗")
                        .description("Professional URLs shortener API developed with Java and Spring Boot.")
                        .version("1.0.0"));
    }
}