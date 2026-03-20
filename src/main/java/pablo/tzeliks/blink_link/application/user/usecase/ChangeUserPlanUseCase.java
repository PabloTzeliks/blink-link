package pablo.tzeliks.blink_link.application.user.usecase;

import pablo.tzeliks.blink_link.domain.common.exception.DomainException;
import pablo.tzeliks.blink_link.domain.user.exception.UserNotFoundException;
import pablo.tzeliks.blink_link.domain.user.model.Plan;
import pablo.tzeliks.blink_link.domain.user.model.User;
import pablo.tzeliks.blink_link.domain.user.ports.UserRepositoryPort;

import java.util.UUID;

public class ChangeUserPlanUseCase {

    private final UserRepositoryPort userRepository;

    public ChangeUserPlanUseCase(UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }

    public void execute(UUID userId, Plan newPlan) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found."));

        user.changePlan(newPlan);

        userRepository.save(user);
    }
}
