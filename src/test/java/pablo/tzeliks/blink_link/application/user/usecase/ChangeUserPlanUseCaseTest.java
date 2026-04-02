package pablo.tzeliks.blink_link.application.user.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pablo.tzeliks.blink_link.domain.user.exception.InvalidPlanException;
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
class ChangeUserPlanUseCaseTest {

    @Mock
    private UserRepositoryPort userRepository;

    @InjectMocks
    private ChangeUserPlanUseCase useCase;

    @Test
    @DisplayName("Should change user plan successfully (Happy Path)")
    void shouldChangeUserPlanSuccessfully() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User user = User.restore(userId, new Email("user@example.com"), new Password("hashed"),
                Role.USER, Plan.FREE, AuthProvider.LOCAL, LocalDateTime.now(), LocalDateTime.now());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.update(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        useCase.execute(userId, Plan.VIP);

        // Assert
        assertEquals(Plan.VIP, user.getPlan());
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
                () -> useCase.execute(userId, Plan.VIP));

        assertEquals("User not found.", exception.getMessage());
        verify(userRepository).findById(userId);
        verify(userRepository, never()).update(any(User.class));
    }

    @Test
    @DisplayName("Should throw InvalidPlanException when user already has the requested plan")
    void shouldThrowExceptionWhenUserAlreadyHasPlan() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User user = User.restore(userId, new Email("user@example.com"), new Password("hashed"),
                Role.USER, Plan.FREE, AuthProvider.LOCAL, LocalDateTime.now(), LocalDateTime.now());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act & Assert
        InvalidPlanException exception = assertThrows(InvalidPlanException.class,
                () -> useCase.execute(userId, Plan.FREE));

        assertEquals("User already has this plan.", exception.getMessage());
        verify(userRepository).findById(userId);
        verify(userRepository, never()).update(any(User.class));
    }

    @Test
    @DisplayName("Should change user plan to ENTERPRISE successfully")
    void shouldChangeUserPlanToEnterprise() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User user = User.restore(userId, new Email("user@example.com"), new Password("hashed"),
                Role.USER, Plan.VIP, AuthProvider.LOCAL, LocalDateTime.now(), LocalDateTime.now());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.update(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        useCase.execute(userId, Plan.ENTERPRISE);

        // Assert
        assertEquals(Plan.ENTERPRISE, user.getPlan());
        verify(userRepository).findById(userId);
        verify(userRepository).update(user);
    }
}
