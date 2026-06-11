package pablo.tzeliks.blink_link.application.user.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pablo.tzeliks.blink_link.application.user.dto.AuthResponse;
import pablo.tzeliks.blink_link.application.user.dto.LoginUserRequest;
import pablo.tzeliks.blink_link.application.user.dto.UserResponse;
import pablo.tzeliks.blink_link.application.user.mapper.UserDtoMapper;
import pablo.tzeliks.blink_link.application.user.ports.TokenGenerationPort;
import pablo.tzeliks.blink_link.domain.user.exception.InvalidEmailException;
import pablo.tzeliks.blink_link.domain.user.exception.InvalidPasswordException;
import pablo.tzeliks.blink_link.domain.user.model.User;
import pablo.tzeliks.blink_link.domain.user.model.valueobject.Email;
import pablo.tzeliks.blink_link.domain.user.model.valueobject.Password;
import pablo.tzeliks.blink_link.domain.user.ports.UserPasswordEncoderPort;
import pablo.tzeliks.blink_link.domain.user.ports.UserRepositoryPort;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginUserUseCaseTest {

    @Mock
    private UserRepositoryPort repositoryPort;

    @Mock
    private UserDtoMapper mapper;

    @Mock
    private UserPasswordEncoderPort passwordEncoder;

    @Mock
    private TokenGenerationPort tokenPort;

    @InjectMocks
    private LoginUserUseCase useCase;

    @Test
    @DisplayName("Should login successfully and return a JWT token with user profile (Happy Path)")
    void shouldLoginSuccessfully() {
        // Arrange
        String email = "user@example.com";
        String rawPassword = "SecurePass123";
        String hashedPassword = "$2a$10$hashedpassword";
        String expectedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.fake.token";
        LocalDateTime now = LocalDateTime.now();

        LoginUserRequest request = new LoginUserRequest(email, rawPassword);

        User existingUser = User.createLocal(new Email(email), new Password(hashedPassword));

        UserResponse expectedUserResponse = new UserResponse(
                existingUser.getId().toString(), email, "USER", "FREE", now, now
        );

        when(repositoryPort.findByEmail(any(Email.class))).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(rawPassword, hashedPassword)).thenReturn(true);
        when(tokenPort.generateToken(existingUser)).thenReturn(expectedToken);
        when(mapper.toDto(existingUser)).thenReturn(expectedUserResponse);

        // Act
        AuthResponse response = useCase.execute(request);

        // Assert
        assertNotNull(response);
        assertEquals(expectedToken, response.token());
        assertNotNull(response.userProfile());
        assertEquals(email, response.userProfile().email());

        verify(repositoryPort).findByEmail(any(Email.class));
        verify(passwordEncoder).matches(rawPassword, hashedPassword);
        verify(tokenPort).generateToken(existingUser);
        verify(mapper).toDto(existingUser);
    }

    @Test
    @DisplayName("Should throw InvalidEmailException when email does not exist")
    void shouldThrowExceptionWhenEmailNotFound() {
        // Arrange
        String email = "nonexistent@example.com";
        LoginUserRequest request = new LoginUserRequest(email, "SomePassword123");

        when(repositoryPort.findByEmail(any(Email.class))).thenReturn(Optional.empty());

        // Act & Assert
        InvalidEmailException exception = assertThrows(InvalidEmailException.class,
                () -> useCase.execute(request));

        assertEquals("Invalid Credentials.", exception.getMessage());

        verify(repositoryPort).findByEmail(any(Email.class));
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verifyNoInteractions(tokenPort);
        verifyNoInteractions(mapper);
    }

    @Test
    @DisplayName("Should throw InvalidPasswordException when password does not match")
    void shouldThrowExceptionWhenPasswordDoesNotMatch() {
        // Arrange
        String email = "user@example.com";
        String rawPassword = "WrongPassword";
        String hashedPassword = "$2a$10$hashedpassword";

        LoginUserRequest request = new LoginUserRequest(email, rawPassword);

        User existingUser = User.createLocal(new Email(email), new Password(hashedPassword));

        when(repositoryPort.findByEmail(any(Email.class))).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(rawPassword, hashedPassword)).thenReturn(false);

        // Act & Assert
        InvalidPasswordException exception = assertThrows(InvalidPasswordException.class,
                () -> useCase.execute(request));

        assertEquals("Invalid Credentials.", exception.getMessage());

        verify(repositoryPort).findByEmail(any(Email.class));
        verify(passwordEncoder).matches(rawPassword, hashedPassword);
        verifyNoInteractions(tokenPort);
        verifyNoInteractions(mapper);
    }

    @Test
    @DisplayName("Should throw InvalidEmailException when email format is invalid")
    void shouldThrowExceptionWhenEmailFormatIsInvalid() {
        // Arrange
        String invalidEmail = "not-an-email";
        LoginUserRequest request = new LoginUserRequest(invalidEmail, "SomePassword123");

        // Act & Assert
        assertThrows(InvalidEmailException.class,
                () -> useCase.execute(request));

        verify(repositoryPort, never()).findByEmail(any(Email.class));
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(tokenPort);
        verifyNoInteractions(mapper);
    }
}
