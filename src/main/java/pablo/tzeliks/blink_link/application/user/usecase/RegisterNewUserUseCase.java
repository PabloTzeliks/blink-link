package pablo.tzeliks.blink_link.application.user.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pablo.tzeliks.blink_link.application.user.dto.RegisterUserRequest;
import pablo.tzeliks.blink_link.application.user.dto.UserResponse;
import pablo.tzeliks.blink_link.application.user.mapper.UserDtoMapper;
import pablo.tzeliks.blink_link.domain.user.exception.InvalidEmailException;
import pablo.tzeliks.blink_link.domain.user.model.User;
import pablo.tzeliks.blink_link.domain.user.model.valueobject.Email;
import pablo.tzeliks.blink_link.domain.user.model.valueobject.Password;
import pablo.tzeliks.blink_link.domain.user.ports.UserPasswordEncoderPort;
import pablo.tzeliks.blink_link.domain.user.ports.UserRepositoryPort;

@Service
public class RegisterNewUserUseCase {

    private final UserRepositoryPort repositoryPort;
    private final UserPasswordEncoderPort passwordEncoderPort;
    private final UserDtoMapper mapper;


    public RegisterNewUserUseCase(UserRepositoryPort repositoryPort, UserPasswordEncoderPort passwordEncoderPort, UserDtoMapper mapper) {
        this.repositoryPort = repositoryPort;
        this.passwordEncoderPort = passwordEncoderPort;
        this.mapper = mapper;
    }

    @Transactional
    public UserResponse execute(RegisterUserRequest request) {

        Email email = new Email(request.email());

        if (repositoryPort.existsByEmail(email)) {

            throw new InvalidEmailException("Email is already in use.");
        }

        String hashedPassword = passwordEncoderPort.encode(request.password());
        Password password = new Password(hashedPassword);

        User newUser = User.createLocal(email, password);

        User savedUser = repositoryPort.save(newUser);

        return mapper.toDto(savedUser);
    }
}
