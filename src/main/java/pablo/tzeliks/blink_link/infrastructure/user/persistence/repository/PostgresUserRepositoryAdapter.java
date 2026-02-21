package pablo.tzeliks.blink_link.infrastructure.user.persistence.repository;

import pablo.tzeliks.blink_link.domain.user.model.User;
import pablo.tzeliks.blink_link.domain.user.model.valueobject.Email;
import pablo.tzeliks.blink_link.domain.user.ports.UserRepositoryPort;

import java.util.Optional;

public class PostgresUserRepositoryAdapter implements UserRepositoryPort {

    @Override
    public User save(User user) {
        return null;
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return Optional.empty();
    }

    @Override
    public boolean existsByEmail(Email email) {
        return false;
    }
}
