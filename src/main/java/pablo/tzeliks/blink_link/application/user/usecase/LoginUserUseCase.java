package pablo.tzeliks.blink_link.application.user.usecase;

import org.springframework.stereotype.Service;
import pablo.tzeliks.blink_link.application.user.dto.AuthResponse;
import pablo.tzeliks.blink_link.application.user.dto.LoginUserRequest;
import pablo.tzeliks.blink_link.application.user.dto.UserResponse;
import pablo.tzeliks.blink_link.application.user.mapper.UserDtoMapper;
import pablo.tzeliks.blink_link.domain.user.exception.InvalidEmailException;
import pablo.tzeliks.blink_link.domain.user.exception.InvalidPasswordException;
import pablo.tzeliks.blink_link.domain.user.model.User;
import pablo.tzeliks.blink_link.domain.user.model.valueobject.Email;
import pablo.tzeliks.blink_link.application.user.ports.TokenGenerationPort;
import pablo.tzeliks.blink_link.domain.user.ports.UserPasswordEncoderPort;
import pablo.tzeliks.blink_link.domain.user.ports.UserRepositoryPort;

@Service
public class LoginUserUseCase {

    private final UserRepositoryPort repositoryPort;
    private final UserDtoMapper mapper;
    private final UserPasswordEncoderPort passwordEncoder;
    private final TokenGenerationPort tokenPort;

    public LoginUserUseCase(UserRepositoryPort repositoryPort,
                            UserDtoMapper mapper,
                            UserPasswordEncoderPort passwordEncoder,
                            TokenGenerationPort tokenPort) {

        this.repositoryPort = repositoryPort;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
        this.tokenPort = tokenPort;
    }

    public AuthResponse execute(LoginUserRequest request) {

        Email email = new Email(request.email());

        User loggedUser = repositoryPort.findByEmail(email)
                .orElseThrow(() -> new InvalidEmailException("Invalid Credentials."));

        boolean passwordMatches = passwordEncoder.matches(request.rawPassword(), loggedUser.getPassword().getValue());

        if (!passwordMatches) {
            throw new InvalidPasswordException("Invalid Credentials.");
        }

        String token = tokenPort.generateToken(loggedUser);

        UserResponse userResponse = mapper.toDto(loggedUser);

        return new AuthResponse(userResponse, token);
    }
}
