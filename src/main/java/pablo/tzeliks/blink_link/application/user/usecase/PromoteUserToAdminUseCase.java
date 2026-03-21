package pablo.tzeliks.blink_link.application.user.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pablo.tzeliks.blink_link.domain.user.exception.UserNotFoundException;
import pablo.tzeliks.blink_link.domain.user.model.User;
import pablo.tzeliks.blink_link.domain.user.ports.UserRepositoryPort;

import java.util.UUID;

@Service
public class PromoteUserToAdminUseCase {

    private final UserRepositoryPort userRepository;

    public PromoteUserToAdminUseCase(UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void execute(UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found."));

        user.promoteToAdmin();

        userRepository.save(user);
    }
}
