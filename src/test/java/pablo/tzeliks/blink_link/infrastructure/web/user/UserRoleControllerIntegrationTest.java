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
public class UserRoleControllerIntegrationTest extends AbstractContainerBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepositoryPort userRepository;

    private CustomUserDetails adminUserDetails() {
        User admin = User.restore(UUID.randomUUID(), new Email("admin@blinklink.com"), new Password("hashed"),
                Role.ADMIN, Plan.FREE, AuthProvider.LOCAL, LocalDateTime.now(), LocalDateTime.now());
        return new CustomUserDetails(admin);
    }

    @Test
    @DisplayName("PATCH /api/v3/users/{id}/promote - Should promote a USER to ADMIN (Happy Path)")
    void shouldPromoteUserToAdmin() throws Exception {
        // Arrange: save a regular user in the DB
        User targetUser = User.createLocal(new Email("target@blinklink.com"), new Password("hashed"));
        userRepository.save(targetUser);

        // Act & Assert
        mockMvc.perform(patch("/api/v3/users/{id}/promote", targetUser.getId())
                        .with(user(adminUserDetails())))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PATCH /api/v3/users/{id}/promote - Should return 404 when user does not exist")
    void shouldReturn404WhenPromotingNonExistentUser() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(patch("/api/v3/users/{id}/promote", nonExistentId)
                        .with(user(adminUserDetails())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource Not Found"));
    }

    @Test
    @DisplayName("PATCH /api/v3/users/{id}/promote - Should return 409 when user is already an ADMIN")
    void shouldReturn409WhenUserIsAlreadyAdmin() throws Exception {
        // Arrange: save an admin user in the DB
        User adminUser = User.restore(UUID.randomUUID(), new Email("existing-admin@blinklink.com"), new Password("hashed"),
                Role.ADMIN, Plan.FREE, AuthProvider.LOCAL, LocalDateTime.now(), LocalDateTime.now());
        userRepository.save(adminUser);

        // Act & Assert
        mockMvc.perform(patch("/api/v3/users/{id}/promote", adminUser.getId())
                        .with(user(adminUserDetails())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Business Rule Error"));
    }

    @Test
    @DisplayName("PATCH /api/v3/users/{id}/promote - Should return 403 when requester is not ADMIN")
    void shouldReturn403WhenNotAdmin() throws Exception {
        UUID anyId = UUID.randomUUID();
        User regularUser = User.restore(UUID.randomUUID(), new Email("user@blinklink.com"), new Password("hashed"),
                Role.USER, Plan.FREE, AuthProvider.LOCAL, LocalDateTime.now(), LocalDateTime.now());
        CustomUserDetails regularUserDetails = new CustomUserDetails(regularUser);

        mockMvc.perform(patch("/api/v3/users/{id}/promote", anyId)
                        .with(user(regularUserDetails)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /api/v3/users/{id}/revoke - Should revoke ADMIN role (Happy Path)")
    void shouldRevokeAdminRole() throws Exception {
        // Arrange: save an admin user in the DB
        User adminUser = User.restore(UUID.randomUUID(), new Email("tobe-revoked@blinklink.com"), new Password("hashed"),
                Role.ADMIN, Plan.FREE, AuthProvider.LOCAL, LocalDateTime.now(), LocalDateTime.now());
        userRepository.save(adminUser);

        // Act & Assert
        mockMvc.perform(patch("/api/v3/users/{id}/revoke", adminUser.getId())
                        .with(user(adminUserDetails())))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PATCH /api/v3/users/{id}/revoke - Should return 404 when user does not exist")
    void shouldReturn404WhenRevokingNonExistentUser() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(patch("/api/v3/users/{id}/revoke", nonExistentId)
                        .with(user(adminUserDetails())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource Not Found"));
    }

    @Test
    @DisplayName("PATCH /api/v3/users/{id}/revoke - Should return 409 when user is already USER (not admin)")
    void shouldReturn409WhenUserIsNotAdmin() throws Exception {
        // Arrange: save a regular user in the DB
        User targetUser = User.createLocal(new Email("regular@blinklink.com"), new Password("hashed"));
        userRepository.save(targetUser);

        // Act & Assert
        mockMvc.perform(patch("/api/v3/users/{id}/revoke", targetUser.getId())
                        .with(user(adminUserDetails())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Business Rule Error"));
    }

    @Test
    @DisplayName("PATCH /api/v3/users/{id}/revoke - Should return 403 when requester is not ADMIN")
    void shouldReturn403WhenNotAdminOnRevoke() throws Exception {
        UUID anyId = UUID.randomUUID();
        User regularUser = User.restore(UUID.randomUUID(), new Email("user@blinklink.com"), new Password("hashed"),
                Role.USER, Plan.FREE, AuthProvider.LOCAL, LocalDateTime.now(), LocalDateTime.now());
        CustomUserDetails regularUserDetails = new CustomUserDetails(regularUser);

        mockMvc.perform(patch("/api/v3/users/{id}/revoke", anyId)
                        .with(user(regularUserDetails)))
                .andExpect(status().isForbidden());
    }
}
