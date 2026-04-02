package pablo.tzeliks.blink_link.application.user.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pablo.tzeliks.blink_link.domain.user.exception.UserAlreadyInRoleException;
import pablo.tzeliks.blink_link.domain.user.exception.UserNotFoundException;
import pablo.tzeliks.blink_link.domain.user.model.AuthProvider;
import pablo.tzeliks.blink_link.domain.user.model.Plan;
import pablo.tzeliks.blink_link.domain.user.model.Role;
import pablo.tzeliks.blink_link.domain.user.model.User;
import pablo.tzeliks.blink_link.domain.user.model.valueobject.Email;
import pablo.tzeliks.blink_link.domain.user.model.valueobject.Password;
import pablo.tzeliks.blink_link.domain.user.ports.UserRepositoryPort;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RevokeUserAdminUseCaseTest {

    @Mock
    private UserRepositoryPort userRepository;

    @InjectMocks
    private RevokeUserAdminUseCase useCase;

    @Test
    @DisplayName("Should revoke ADMIN role and return user to USER successfully (Happy Path)")
    void shouldRevokeAdminSuccessfully() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User adminUser = User.restore(userId, new Email("admin@example.com"), new Password("hashed"),
                Role.ADMIN, Plan.FREE, AuthProvider.LOCAL, LocalDateTime.now(), LocalDateTime.now());

        when(userRepository.findById(userId)).thenReturn(Optional.of(adminUser));
        when(userRepository.update(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        useCase.execute(userId);

        // Assert
        assertEquals(Role.USER, adminUser.getRole());
        verify(userRepository).findById(userId);
        verify(userRepository).update(adminUser);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when user does not exist")
    void shouldThrowExceptionWhenUserNotFound() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> useCase.execute(userId));

        assertEquals("User not found.", exception.getMessage());
        verify(userRepository).findById(userId);
        verify(userRepository, never()).update(any(User.class));
    }

    @Test
    @DisplayName("Should throw UserAlreadyInRoleException when user is already a USER (not admin)")
    void shouldThrowExceptionWhenUserIsAlreadyNotAdmin() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User regularUser = User.restore(userId, new Email("user@example.com"), new Password("hashed"),
                Role.USER, Plan.FREE, AuthProvider.LOCAL, LocalDateTime.now(), LocalDateTime.now());

        when(userRepository.findById(userId)).thenReturn(Optional.of(regularUser));

        // Act & Assert
        assertThrows(UserAlreadyInRoleException.class, () -> useCase.execute(userId));

        verify(userRepository).findById(userId);
        verify(userRepository, never()).update(any(User.class));
    }
}
