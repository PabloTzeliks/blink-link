package pablo.tzeliks.blink_link.infrastructure.web.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pablo.tzeliks.blink_link.infrastructure.AbstractContainerBase;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthAndUrlIntegrationTest extends AbstractContainerBase {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static Cookie jwtCookie;
    private static String createdShortCode;

    @Test
    @Order(1)
    @DisplayName("E2E: POST /api/v3/auth/register - Should register a new user")
    void shouldRegisterUser() throws Exception {
        String registerJson = """
                {
                    "email": "e2e@blinklink.com",
                    "password": "SuperSecret123"
                }
                """;

        mockMvc.perform(post("/api/v3/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value("e2e@blinklink.com"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.plan").value("FREE"));
    }

    @Test
    @Order(2)
    @DisplayName("E2E: POST /api/v3/auth/login - Should login and receive JWT via HttpOnly cookie")
    void shouldLoginAndReceiveJwtCookie() throws Exception {
        String loginJson = """
                {
                    "email": "e2e@blinklink.com",
                    "password": "SuperSecret123"
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/v3/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(header().exists("Set-Cookie"))
                .andExpect(header().string("Set-Cookie", containsString("jwt_token=")))
                .andExpect(header().string("Set-Cookie", containsString("HttpOnly")))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value("e2e@blinklink.com"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.plan").value("FREE"))
                .andExpect(jsonPath("$.token").doesNotExist())
                .andReturn();

        jwtCookie = result.getResponse().getCookie("jwt_token");
        assertNotNull(jwtCookie, "jwt_token cookie must be present in the login response");
        assertTrue(jwtCookie.isHttpOnly(), "jwt_token cookie must be HttpOnly");
    }

    @Test
    @Order(3)
    @DisplayName("E2E: POST /api/v3/urls/shorten - Should create shortened URL with cookie auth")
    void shouldShortenUrlWithCookieAuth() throws Exception {
        String urlJson = """
                {
                    "original_url": "https://github.com/PabloTzeliks/blink-link"
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/v3/urls/shorten")
                        .cookie(jwtCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(urlJson))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.original_url").value("https://github.com/PabloTzeliks/blink-link"))
                .andExpect(jsonPath("$.short_code").exists())
                .andExpect(jsonPath("$.created_at").exists())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        createdShortCode = objectMapper.readTree(responseBody).get("short_code").asText();
    }

    @Test
    @Order(4)
    @DisplayName("E2E: GET /api/v3/urls/{shortCode} - Should retrieve URL details with cookie auth")
    void shouldRetrieveUrlDetailsWithCookieAuth() throws Exception {
        mockMvc.perform(get("/api/v3/urls/" + createdShortCode)
                        .cookie(jwtCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.original_url").value("https://github.com/PabloTzeliks/blink-link"))
                .andExpect(jsonPath("$.short_code").value(createdShortCode));
    }

    @Test
    @Order(5)
    @DisplayName("E2E: GET /{shortCode} - Should redirect to original URL (public endpoint)")
    void shouldRedirectToOriginalUrl() throws Exception {
        mockMvc.perform(get("/" + createdShortCode))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://github.com/PabloTzeliks/blink-link"));
    }

    @Test
    @Order(6)
    @DisplayName("E2E: POST /api/v3/urls/shorten - Should return 401 without authentication")
    void shouldReturn401WithoutAuthentication() throws Exception {
        String urlJson = """
                {
                    "original_url": "https://google.com"
                }
                """;

        mockMvc.perform(post("/api/v3/urls/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(urlJson))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(7)
    @DisplayName("E2E: POST /api/v3/auth/register - Should return 401 for duplicate email")
    void shouldReturn401ForDuplicateRegistration() throws Exception {
        String registerJson = """
                {
                    "email": "e2e@blinklink.com",
                    "password": "AnotherPassword456"
                }
                """;

        mockMvc.perform(post("/api/v3/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(8)
    @DisplayName("E2E: POST /api/v3/auth/login - Should return 401 for wrong password")
    void shouldReturn401ForWrongPassword() throws Exception {
        String loginJson = """
                {
                    "email": "e2e@blinklink.com",
                    "password": "WrongPassword999"
                }
                """;

        mockMvc.perform(post("/api/v3/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Invalid Credentials"));
    }

    @Test
    @Order(9)
    @DisplayName("E2E: POST /api/v3/auth/logout - Should clear the jwt_token cookie")
    void shouldLogoutAndClearCookie() throws Exception {
        mockMvc.perform(post("/api/v3/auth/logout")
                        .cookie(jwtCookie))
                .andExpect(status().isOk())
                .andExpect(header().exists("Set-Cookie"))
                .andExpect(header().string("Set-Cookie", containsString("jwt_token=")))
                .andExpect(header().string("Set-Cookie", containsString("Max-Age=0")));
    }
}
