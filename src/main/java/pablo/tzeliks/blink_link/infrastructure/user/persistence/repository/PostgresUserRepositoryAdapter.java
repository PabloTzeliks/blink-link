package pablo.tzeliks.blink_link.infrastructure.user.persistence.repository;

import pablo.tzeliks.blink_link.domain.user.model.User;
import pablo.tzeliks.blink_link.domain.user.model.valueobject.Email;
import pablo.tzeliks.blink_link.domain.user.ports.UserRepositoryPort;
import pablo.tzeliks.blink_link.infrastructure.user.persistence.entity.UserEntity;
import pablo.tzeliks.blink_link.infrastructure.user.persistence.mapper.UserEntityMapper;

import java.util.Optional;

public class PostgresUserRepositoryAdapter implements UserRepositoryPort {

    private final JpaUserRepository repository;
    private final UserEntityMapper mapper;

    public PostgresUserRepositoryAdapter(JpaUserRepository repository, UserEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public User save(User user) {

        Optional<UserEntity> existingEntityOpt = repository.findById(user.getId());

        if (existingEntityOpt.isPresent()) {

            // Update flow
            UserEntity existingEntity = existingEntityOpt.get();
            mapper.updateEntityFromDomain(user, existingEntity);

            UserEntity savedEntity = repository.save(existingEntity);

            return mapper.toDomain(savedEntity);
        } else {

            // Insert flow
            UserEntity newEntity = mapper.toEntity(user);
            UserEntity savedEntity = repository.save(newEntity);

            return mapper.toDomain(savedEntity);
        }
    }

    @Override
    public Optional<User> findByEmail(Email email) {

        return repository.findByEmail(email.getValue())
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(Email email) {

        return repository.existsByEmail(email.getValue());
    }
}
