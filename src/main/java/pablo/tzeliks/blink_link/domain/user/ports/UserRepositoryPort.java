package pablo.tzeliks.blink_link.domain.user.ports;

import pablo.tzeliks.blink_link.domain.user.model.User;
import pablo.tzeliks.blink_link.domain.user.model.valueobject.Email;

import java.util.Optional;

public interface UserRepositoryPort {

    User save(User user);
    Optional<User> findByEmail(Email email);
    boolean existsByEmail(Email email);
}
