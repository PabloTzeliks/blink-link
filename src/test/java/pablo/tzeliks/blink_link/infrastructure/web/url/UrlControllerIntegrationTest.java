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
import pablo.tzeliks.blink_link.domain.user.model.AuthProvider;
import pablo.tzeliks.blink_link.domain.user.model.Plan;
import pablo.tzeliks.blink_link.domain.user.model.Role;
import pablo.tzeliks.blink_link.domain.user.model.User;
import pablo.tzeliks.blink_link.domain.user.model.valueobject.Email;
import pablo.tzeliks.blink_link.domain.user.model.valueobject.Password;
import pablo.tzeliks.blink_link.infrastructure.AbstractContainerBase;
import pablo.tzeliks.blink_link.infrastructure.security.adapter.CustomUserDetails;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the URL controller REST API endpoints.
 * <p>
 * This test class validates the complete HTTP request-response cycle for both
 * {@link UrlController} and {@link RedirectUrlController}, testing the controllers
 * along with the entire application stack including service layer, repository layer,
 * and database operations.
 * <p>
 * <b>Test Strategy:</b>
 * <p>
 * These are full integration tests that:
 * <ul>
 *   <li>Use a real PostgreSQL database via Testcontainers</li>
 *   <li>Load the complete Spring application context</li>
 *   <li>Simulate HTTP requests using MockMvc</li>
 *   <li>Validate HTTP responses, status codes, and JSON payloads</li>
 *   <li>Test both happy paths and error scenarios</li>
 * </ul>
 * <p>
 * <b>Testcontainers:</b>
 * <p>
 * By extending {@link AbstractContainerBase}, these tests run against a real
 * PostgreSQL 17 container, providing high confidence that the code works correctly
 * with the actual database. This is more reliable than using in-memory databases
 * (like H2) that may behave differently from production.
 * <p>
 * <b>Test Coverage:</b>
 * <ul>
 *   <li>URL creation (POST /api/v3/urls/shorten) - Happy path and validation errors</li>
 *   <li>URL retrieval (GET /api/v3/urls/{shortCode}) - Success and 404 scenarios</li>
 *   <li>URL redirection (GET /{shortCode}) - Success and 404 scenarios</li>
 *   <li>Malformed JSON handling</li>
 * </ul>
 * <p>
 * <b>Transaction Rollback:</b>
 * <p>
 * The {@code @Transactional} annotation ensures that each test method runs in a
 * transaction that is rolled back after completion, keeping tests isolated and
 * preventing data pollution between tests.
 *
 * @author Pablo Tzeliks
 * @version 2.0.0
 * @since 1.0.0
 * @see UrlController
 * @see RedirectUrlController
 * @see AbstractContainerBase
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class UrlControllerIntegrationTest extends AbstractContainerBase {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UrlRepositoryPort repository;

    /**
     * Integration Test: Verifies successful URL shortening via POST endpoint.
     * <p>
     * <b>Scenario:</b> Happy Path - Valid URL submission
     * <p>
     * <b>Given:</b> A valid long URL (LinkedIn profile)
     * <br><b>When:</b> POST request is made to /api/v3/urls/shorten
     * <br><b>Then:</b> API returns 201 Created with complete URL response including
     * original URL, short code, full short URL, and creation timestamp. The Location
     * header contains the URI of the newly created resource.
     * <p>
     * <b>Assertions:</b>
     * <ul>
     *   <li>HTTP status is 201 (Created)</li>
     *   <li>Location header is present</li>
     *   <li>Response contains all expected fields</li>
     *   <li>Short code is generated and is a non-empty string</li>
     * </ul>
     */
    @Test
    @DisplayName("POST /shorten - Should create a short URL successfully (Happy Path)")
    void shouldCreateShortUrl() throws Exception {
        // Arrange
        CreateUrlRequest request = new CreateUrlRequest("https://www.linkedin.com/in/pablo-ruan-tzeliks/");
        String jsonRequest = objectMapper.writeValueAsString(request);

        User domainUser = User.restore(UUID.randomUUID(), new Email("test@test.com"), new Password("encoded"),
                Role.USER, Plan.FREE, AuthProvider.LOCAL, LocalDateTime.now(), LocalDateTime.now());
        CustomUserDetails userDetails = new CustomUserDetails(domainUser);

        // Act & Assert
        mockMvc.perform(post("/api/v3/urls/shorten")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.original_url").value("https://www.linkedin.com/in/pablo-ruan-tzeliks/"))
                .andExpect(jsonPath("$.short_code").exists())
                .andExpect(jsonPath("$.short_code").isString())
                .andExpect(jsonPath("$.created_at").exists());
    }

    /**
     * Integration Test: Verifies validation error handling for invalid URLs.
     * <p>
     * <b>Scenario:</b> Validation Failure - Empty URL parameter
     * <p>
     * <b>Given:</b> A request with an empty originalUrl field
     * <br><b>When:</b> POST request is made to /api/v3/urls/shorten
     * <br><b>Then:</b> API returns 422 Unprocessable Content with validation error details
     * <p>
     * <b>Assertions:</b>
     * <ul>
     *   <li>HTTP status is 422 (Unprocessable Content)</li>
     *   <li>Error response title is "Validation Failed"</li>
     *   <li>Error response includes at least one validation error</li>
     * </ul>
     * <p>
     * This tests the {@code @Valid} annotation on the controller and the
     * {@code @NotBlank} constraint on the DTO.
     */
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
        mockMvc.perform(post("/api/v3/urls/shorten")
                        .with(user("test@test.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isUnprocessableEntity()) // 422
                .andExpect(jsonPath("$.title").value("Validation Failed"))
                .andExpect(jsonPath("$.errors", hasSize(greaterThan(0))));
    }

    /**
     * Integration Test: Verifies malformed JSON error handling.
     * <p>
     * <b>Scenario:</b> Malformed JSON - Syntax error in request body
     * <p>
     * <b>Given:</b> A JSON string with syntax errors (missing closing quote)
     * <br><b>When:</b> POST request is made to /api/v3/urls/shorten
     * <br><b>Then:</b> API returns 400 Bad Request with malformed JSON error message
     * <p>
     * <b>Assertions:</b>
     * <ul>
     *   <li>HTTP status is 400 (Bad Request)</li>
     *   <li>Error response title is "Malformed JSON Request"</li>
     * </ul>
     * <p>
     * This tests the {@link GlobalExceptionHandler}'s handling of
     * {@code HttpMessageNotReadableException}.
     */
    @Test
    @DisplayName("POST /shorten - Should return 400 when JSON is malformed (Sad Path: JSON Error)")
    void shouldReturn400ForMalformedJson() throws Exception {
        // Arrange: broken JSON (missing quote)
        String brokenJson = "{ \"original_url\": \"https://google.com }";

        // Act & Assert
        mockMvc.perform(post("/api/v3/urls/shorten")
                        .with(user("test@test.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(brokenJson))
                .andExpect(status().isBadRequest()) // 400
                .andExpect(jsonPath("$.title").value("Malformed JSON Request"));
    }

    /**
     * Integration Test: Verifies URL details retrieval by short code.
     * <p>
     * <b>Scenario:</b> Happy Path - Retrieve existing URL information
     * <p>
     * <b>Given:</b> A URL has been pre-inserted into the database with short code "rocket"
     * <br><b>When:</b> GET request is made to /api/v3/urls/rocket
     * <br><b>Then:</b> API returns 200 OK with complete URL details
     * <p>
     * <b>Assertions:</b>
     * <ul>
     *   <li>HTTP status is 200 (OK)</li>
     *   <li>Response contains the correct original URL</li>
     *   <li>Response contains the correct short code</li>
     * </ul>
     * <p>
     * This test validates the informational endpoint that returns URL metadata
     * without performing a redirect.
     */
    @Test
    @DisplayName("GET /api/v3/urls/{ShortCode} - Should return URL details (Happy Path)")
    void shouldReturnUrlDetails() throws Exception {
        // Arrange: Pre-insert an URL into the database
        Long id = repository.nextId();
        LocalDateTime now = LocalDateTime.now();

        Url savedUrl = Url.restore(
                id,
                "https://rocketseat.com.br",
                "rocket",
                now,
                now.plusDays(7)
        );

        repository.save(savedUrl);

        // Act & Assert
        mockMvc.perform(get("/api/v3/urls/" + savedUrl.getShortCode())
                        .with(user("test@test.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.original_url").value("https://rocketseat.com.br"))
                .andExpect(jsonPath("$.short_code").value("rocket"));
    }

    /**
     * Integration Test: Verifies 404 response for non-existent short code.
     * <p>
     * <b>Scenario:</b> Error Case - Short code does not exist in database
     * <p>
     * <b>Given:</b> A short code "ghost-code-123" that doesn't exist
     * <br><b>When:</b> GET request is made to /api/v3/urls/ghost-code-123
     * <br><b>Then:</b> API returns 404 Not Found with error details
     * <p>
     * <b>Assertions:</b>
     * <ul>
     *   <li>HTTP status is 404 (Not Found)</li>
     *   <li>Error response title is "Resource Not Found"</li>
     * </ul>
     * <p>
     * This tests the {@link GlobalExceptionHandler}'s handling of
     * {@code UrlNotFoundException}.
     */
    @Test
    @DisplayName("GET /api/v3/urls/{ShortCode} - Should return 404 for non-existent code (Sad Path)")
    void shouldReturn404ForDetailsOfGhostCode() throws Exception {
        mockMvc.perform(get("/api/v3/urls/ghost-code-123")
                        .with(user("test@test.com").roles("USER")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource Not Found"));
    }

    // RedirectUrlController Tests

    /**
     * Integration Test: Verifies successful URL redirection via short code.
     * <p>
     * <b>Scenario:</b> Happy Path - Redirect to original URL
     * <p>
     * <b>Given:</b> A URL has been pre-inserted with short code "myGit"
     * <br><b>When:</b> GET request is made to /myGit
     * <br><b>Then:</b> API returns 302 Found with Location header pointing to original URL
     * <p>
     * <b>Assertions:</b>
     * <ul>
     *   <li>HTTP status is 302 (Found - Temporary Redirect)</li>
     *   <li>Location header contains the original URL</li>
     * </ul>
     * <p>
     * This is the primary user-facing functionality: when someone clicks a shortened
     * URL, they are transparently redirected to the original destination.
     */
    @Test
    @DisplayName("GET /{shortUrl} - Should redirect to original URL (Happy Path)")
    void shouldRedirectSuccessfully() throws Exception {
        // Arrange
        Long id = repository.nextId();
        LocalDateTime now = LocalDateTime.now();
        Url savedUrl = Url.restore(
                id,
                "https://github.com/PabloTzeliks",
                "myGit",
                now,
                now.plusDays(7)
        );
        repository.save(savedUrl);

        // Act & Assert
        mockMvc.perform(get("/myGit"))
                .andExpect(status().isFound()) // 302
                .andExpect(header().string("Location", "https://github.com/PabloTzeliks"));
    }

    /**
     * Integration Test: Verifies 404 response when redirecting non-existent short code.
     * <p>
     * <b>Scenario:</b> Error Case - Attempt to redirect using invalid short code
     * <p>
     * <b>Given:</b> A short code "nao-existe" that doesn't exist in the database
     * <br><b>When:</b> GET request is made to /nao-existe
     * <br><b>Then:</b> API returns 404 Not Found instead of attempting a redirect
     * <p>
     * <b>Assertions:</b>
     * <ul>
     *   <li>HTTP status is 404 (Not Found)</li>
     *   <li>Error response title is "Resource Not Found"</li>
     * </ul>
     * <p>
     * This ensures users get clear feedback when clicking on invalid or expired
     * shortened URLs rather than being redirected to an error page or broken link.
     */
    @Test
    @DisplayName("GET /{shortUrl} - Should return 404 when redirecting non-existent code (Sad Path)")
    void shouldReturn404ForRedirectingGhostCode() throws Exception {
        mockMvc.perform(get("/nao-existe"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource Not Found"));
    }
}