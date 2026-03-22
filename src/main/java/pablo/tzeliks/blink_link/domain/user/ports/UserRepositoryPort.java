package pablo.tzeliks.blink_link.domain.user.ports;

import pablo.tzeliks.blink_link.domain.user.model.User;
import pablo.tzeliks.blink_link.domain.user.model.valueobject.Email;

import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {

    User save(User user);

    User update(User user);

    Optional<User> findByEmail(Email email);

    Optional<User> findById(UUID id);

    boolean existsByEmail(Email email);
}
