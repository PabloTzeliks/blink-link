package pablo.tzeliks.blink_link.application.user.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pablo.tzeliks.blink_link.domain.user.model.AuthProvider;
import pablo.tzeliks.blink_link.domain.user.model.User;
import pablo.tzeliks.blink_link.domain.user.model.valueobject.Email;
import pablo.tzeliks.blink_link.domain.user.ports.UserRepositoryPort;

import java.util.Optional;

@Service
public class ProcessOAuth2UserUseCase {

    private final UserRepositoryPort userRepository;

    public ProcessOAuth2UserUseCase(UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User execute(Email email, AuthProvider provider) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            return userOptional.get();
        }

        User newUser = User.createOAuth2(email, provider);
        return userRepository.save(newUser);
    }
}
