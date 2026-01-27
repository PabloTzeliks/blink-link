package pablo.tzeliks.blink_link.infrastructure.web.url;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pablo.tzeliks.blink_link.application.url.dto.CreateUrlRequest;
import pablo.tzeliks.blink_link.domain.url.model.Url;
import pablo.tzeliks.blink_link.domain.url.ports.UrlRepositoryPort;
import pablo.tzeliks.blink_link.infrastructure.persistence.AbstractContainerBase;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class UrlControllerIntegrationTest extends AbstractContainerBase {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UrlRepositoryPort repository;

    @Test
    @DisplayName("POST /shorten - Should create a short URL successfully (Happy Path)")
    void shouldCreateShortUrl() throws Exception {
        // Arrange
        CreateUrlRequest request = new CreateUrlRequest("https://www.linkedin.com/in/pablo-ruan-tzeliks/");
        String jsonRequest = objectMapper.writeValueAsString(request);

        // Act & Assert
        mockMvc.perform(post("/api/v2/urls/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.original_url").value("https://www.linkedin.com/in/pablo-ruan-tzeliks/"))
                .andExpect(jsonPath("$.short_code").exists())
                .andExpect(jsonPath("$.short_code").isString())
                .andExpect(jsonPath("$.created_at").exists());
    }

    @Test
    @DisplayName("POST /shorten - Should return 422 when URL is invalid (Sad Path: Validation)")
    void shouldReturn422ForInvalidUrl() throws Exception {
        // Arrange: Empty parameter
        String invalidJson = """
                {
                    "originalUrl": ""
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/v2/urls/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isUnprocessableEntity()) // 422
                .andExpect(jsonPath("$.title").value("Validation Failed"))
                .andExpect(jsonPath("$.errors", hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("POST /shorten - Should return 400 when JSON is malformed (Sad Path: JSON Error)")
    void shouldReturn400ForMalformedJson() throws Exception {
        // Arrange: broken JSON (missing quote)
        String brokenJson = "{ \"original_url\": \"https://google.com }";

        // Act & Assert
        mockMvc.perform(post("/api/v2/urls/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(brokenJson))
                .andExpect(status().isBadRequest()) // 400
                .andExpect(jsonPath("$.title").value("Malformed JSON Request"));
    }

    @Test
    @DisplayName("GET /api/v2/urls/{ShortCode} - Should return URL details (Happy Path)")
    void shouldReturnUrlDetails() throws Exception {
        // Arrange: Pre-insert an URL into the database
        Long id = repository.nextId();
        LocalDateTime now = LocalDateTime.now();

        Url savedUrl = new Url(
                id,
                "https://rocketseat.com.br",
                "rocket",
                now
        );

        repository.save(savedUrl);

        // Act & Assert
        mockMvc.perform(get("/api/v2/urls/" + savedUrl.getShortCode()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.original_url").value("https://rocketseat.com.br"))
                .andExpect(jsonPath("$.short_code").value("rocket"));
    }

    @Test
    @DisplayName("GET /api/v2/urls/{ShortCode} - Should return 404 for non-existent code (Sad Path)")
    void shouldReturn404ForDetailsOfGhostCode() throws Exception {
        mockMvc.perform(get("/api/v2/urls/ghost-code-123"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource Not Found"));
    }

    // RedirectUrlController

    @Test
    @DisplayName("GET /{shortUrl} - Should redirect to original URL (Happy Path)")
    void shouldRedirectSuccessfully() throws Exception {
        // Arrange
        Long id = repository.nextId();
        Url savedUrl = new Url(
                id,
                "https://github.com/PabloTzeliks",
                "myGit",
                LocalDateTime.now()
        );
        repository.save(savedUrl);

        // Act & Assert
        mockMvc.perform(get("/myGit"))
                .andExpect(status().isFound()) // 302
                .andExpect(header().string("Location", "https://github.com/PabloTzeliks"));
    }

    @Test
    @DisplayName("GET /{shortUrl} - Should return 404 when redirecting non-existent code (Sad Path)")
    void shouldReturn404ForRedirectingGhostCode() throws Exception {
        mockMvc.perform(get("/nao-existe"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource Not Found"));
    }
}