package pablo.tzeliks.blink_link.application.user.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pablo.tzeliks.blink_link.application.user.dto.RegisterUserRequest;
import pablo.tzeliks.blink_link.application.user.dto.UserResponse;
import pablo.tzeliks.blink_link.application.user.mapper.UserDtoMapper;
import pablo.tzeliks.blink_link.domain.user.exception.InvalidEmailException;
import pablo.tzeliks.blink_link.domain.user.model.User;
import pablo.tzeliks.blink_link.domain.user.model.valueobject.Email;
import pablo.tzeliks.blink_link.domain.user.ports.UserPasswordEncoderPort;
import pablo.tzeliks.blink_link.domain.user.ports.UserRepositoryPort;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterNewUserUseCaseTest {

    @Mock
    private UserRepositoryPort repositoryPort;

    @Mock
    private UserPasswordEncoderPort passwordEncoderPort;

    @Mock
    private UserDtoMapper mapper;

    @InjectMocks
    private RegisterNewUserUseCase useCase;

    @Test
    @DisplayName("Should register a new user successfully (Happy Path)")
    void shouldRegisterNewUserSuccessfully() {
        // Arrange
        String email = "test@example.com";
        String rawPassword = "SecurePass123";
        String hashedPassword = "$2a$10$hashedpassword";
        RegisterUserRequest request = new RegisterUserRequest(email, rawPassword);
        LocalDateTime now = LocalDateTime.now();

        UserResponse expectedResponse = new UserResponse(
                "some-uuid", email, "USER", "FREE", now, now
        );

        when(repositoryPort.existsByEmail(any(Email.class))).thenReturn(false);
        when(passwordEncoderPort.encode(rawPassword)).thenReturn(hashedPassword);
        when(repositoryPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toDto(any(User.class))).thenReturn(expectedResponse);

        // Act
        UserResponse actualResponse = useCase.execute(request);

        // Assert
        assertNotNull(actualResponse);
        assertEquals(email, actualResponse.email());
        assertEquals("USER", actualResponse.role());
        assertEquals("FREE", actualResponse.plan());

        verify(repositoryPort).existsByEmail(any(Email.class));
        verify(passwordEncoderPort).encode(rawPassword);
        verify(repositoryPort).save(any(User.class));
        verify(mapper).toDto(any(User.class));
    }

    @Test
    @DisplayName("Should throw InvalidEmailException when email is already in use")
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        // Arrange
        String email = "existing@example.com";
        RegisterUserRequest request = new RegisterUserRequest(email, "SomePassword123");

        when(repositoryPort.existsByEmail(any(Email.class))).thenReturn(true);

        // Act & Assert
        InvalidEmailException exception = assertThrows(InvalidEmailException.class,
                () -> useCase.execute(request));

        assertEquals("Email is already in use.", exception.getMessage());

        verify(repositoryPort).existsByEmail(any(Email.class));
        verify(passwordEncoderPort, never()).encode(anyString());
        verify(repositoryPort, never()).save(any(User.class));
        verifyNoInteractions(mapper);
    }

    @Test
    @DisplayName("Should throw InvalidEmailException when email format is invalid")
    void shouldThrowExceptionWhenEmailFormatIsInvalid() {
        // Arrange
        String invalidEmail = "not-an-email";
        RegisterUserRequest request = new RegisterUserRequest(invalidEmail, "SomePassword123");

        // Act & Assert
        assertThrows(InvalidEmailException.class,
                () -> useCase.execute(request));

        verifyNoInteractions(passwordEncoderPort);
        verify(repositoryPort, never()).save(any(User.class));
        verifyNoInteractions(mapper);
    }
}
