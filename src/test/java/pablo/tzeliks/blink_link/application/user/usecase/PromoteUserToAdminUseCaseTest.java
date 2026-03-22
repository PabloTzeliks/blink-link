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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PromoteUserToAdminUseCaseTest {

    @Mock
    private UserRepositoryPort userRepository;

    @InjectMocks
    private PromoteUserToAdminUseCase useCase;

    @Test
    @DisplayName("Should promote a USER to ADMIN successfully (Happy Path)")
    void shouldPromoteUserToAdminSuccessfully() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User user = User.restore(userId, new Email("user@example.com"), new Password("hashed"),
                Role.USER, Plan.FREE, AuthProvider.LOCAL, LocalDateTime.now(), LocalDateTime.now());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.update(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        useCase.execute(userId);

        // Assert
        assertEquals(Role.ADMIN, user.getRole());
        verify(userRepository).findById(userId);
        verify(userRepository).update(user);
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
    @DisplayName("Should throw UserAlreadyInRoleException when user is already an ADMIN")
    void shouldThrowExceptionWhenUserIsAlreadyAdmin() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User adminUser = User.restore(userId, new Email("admin@example.com"), new Password("hashed"),
                Role.ADMIN, Plan.FREE, AuthProvider.LOCAL, LocalDateTime.now(), LocalDateTime.now());

        when(userRepository.findById(userId)).thenReturn(Optional.of(adminUser));

        // Act & Assert
        assertThrows(UserAlreadyInRoleException.class, () -> useCase.execute(userId));

        verify(userRepository).findById(userId);
        verify(userRepository, never()).update(any(User.class));
    }
}
