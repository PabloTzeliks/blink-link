package pablo.tzeliks.blink_link.infrastructure.web.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pablo.tzeliks.blink_link.domain.user.model.AuthProvider;
import pablo.tzeliks.blink_link.domain.user.model.Plan;
import pablo.tzeliks.blink_link.domain.user.model.Role;
import pablo.tzeliks.blink_link.domain.user.model.User;
import pablo.tzeliks.blink_link.domain.user.model.valueobject.Email;
import pablo.tzeliks.blink_link.domain.user.model.valueobject.Password;
import pablo.tzeliks.blink_link.domain.user.ports.UserRepositoryPort;
import pablo.tzeliks.blink_link.infrastructure.AbstractContainerBase;
import pablo.tzeliks.blink_link.infrastructure.security.adapter.CustomUserDetails;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class UserPlanControllerIntegrationTest extends AbstractContainerBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepositoryPort userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private CustomUserDetails adminUserDetails() {
        User admin = User.restore(UUID.randomUUID(), new Email("admin@blinklink.com"), new Password("hashed"),
                Role.ADMIN, Plan.FREE, AuthProvider.LOCAL, LocalDateTime.now(), LocalDateTime.now());
        return new CustomUserDetails(admin);
    }

    @Test
    @DisplayName("PATCH /api/v3/users/{id}/plan - Should change user plan successfully (Happy Path)")
    void shouldChangeUserPlanSuccessfully() throws Exception {
        // Arrange: save a FREE plan user in the DB
        User targetUser = User.createLocal(new Email("plan-user@blinklink.com"), new Password("hashed"));
        userRepository.save(targetUser);

        String requestBody = """
                {
                    "plan": "VIP"
                }
                """;

        // Act & Assert
        mockMvc.perform(patch("/api/v3/users/{id}/plan", targetUser.getId())
                        .with(user(adminUserDetails()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PATCH /api/v3/users/{id}/plan - Should return 404 when user does not exist")
    void shouldReturn404WhenChangingPlanForNonExistentUser() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        String requestBody = """
                {
                    "plan": "VIP"
                }
                """;

        mockMvc.perform(patch("/api/v3/users/{id}/plan", nonExistentId)
                        .with(user(adminUserDetails()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource Not Found"));
    }

    @Test
    @DisplayName("PATCH /api/v3/users/{id}/plan - Should return 400 when user already has the requested plan")
    void shouldReturn400WhenUserAlreadyHasPlan() throws Exception {
        // Arrange: save a FREE plan user in the DB
        User targetUser = User.createLocal(new Email("same-plan-user@blinklink.com"), new Password("hashed"));
        userRepository.save(targetUser);

        String requestBody = """
                {
                    "plan": "FREE"
                }
                """;

        // Act & Assert (InvalidPlanException extends InvalidResourceException -> 400 Bad Request)
        mockMvc.perform(patch("/api/v3/users/{id}/plan", targetUser.getId())
                        .with(user(adminUserDetails()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid Argument"));
    }

    @Test
    @DisplayName("PATCH /api/v3/users/{id}/plan - Should return 400 when plan name is invalid")
    void shouldReturn400WhenPlanNameIsInvalid() throws Exception {
        User targetUser = User.createLocal(new Email("invalid-plan-user@blinklink.com"), new Password("hashed"));
        userRepository.save(targetUser);

        String requestBody = """
                {
                    "plan": "GOLD"
                }
                """;

        mockMvc.perform(patch("/api/v3/users/{id}/plan", targetUser.getId())
                        .with(user(adminUserDetails()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /api/v3/users/{id}/plan - Should return 422 when plan field is blank")
    void shouldReturn422WhenPlanFieldIsBlank() throws Exception {
        User targetUser = User.createLocal(new Email("blank-plan-user@blinklink.com"), new Password("hashed"));
        userRepository.save(targetUser);

        String requestBody = """
                {
                    "plan": ""
                }
                """;

        mockMvc.perform(patch("/api/v3/users/{id}/plan", targetUser.getId())
                        .with(user(adminUserDetails()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.title").value("Validation Failed"));
    }

    @Test
    @DisplayName("PATCH /api/v3/users/{id}/plan - Should return 403 when requester is not ADMIN")
    void shouldReturn403WhenNotAdmin() throws Exception {
        UUID anyId = UUID.randomUUID();
        User regularUser = User.restore(UUID.randomUUID(), new Email("user@blinklink.com"), new Password("hashed"),
                Role.USER, Plan.FREE, AuthProvider.LOCAL, LocalDateTime.now(), LocalDateTime.now());
        CustomUserDetails regularUserDetails = new CustomUserDetails(regularUser);

        String requestBody = """
                {
                    "plan": "VIP"
                }
                """;

        mockMvc.perform(patch("/api/v3/users/{id}/plan", anyId)
                        .with(user(regularUserDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /api/v3/users/{id}/plan - Should change plan to ENTERPRISE successfully")
    void shouldChangePlanToEnterprise() throws Exception {
        User targetUser = User.createLocal(new Email("enterprise-user@blinklink.com"), new Password("hashed"));
        userRepository.save(targetUser);

        String requestBody = """
                {
                    "plan": "ENTERPRISE"
                }
                """;

        mockMvc.perform(patch("/api/v3/users/{id}/plan", targetUser.getId())
                        .with(user(adminUserDetails()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNoContent());
    }
}
