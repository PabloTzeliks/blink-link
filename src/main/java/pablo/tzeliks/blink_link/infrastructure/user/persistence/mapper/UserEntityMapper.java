package pablo.tzeliks.blink_link.infrastructure.user.persistence.mapper;

import org.springframework.stereotype.Component;
import pablo.tzeliks.blink_link.domain.user.model.User;
import pablo.tzeliks.blink_link.domain.user.model.valueobject.Email;
import pablo.tzeliks.blink_link.domain.user.model.valueobject.Password;
import pablo.tzeliks.blink_link.infrastructure.user.persistence.entity.UserEntity;

@Component
public class UserEntityMapper {

    public UserEntity toEntity(User domain) {

        return new UserEntity(
                domain.getId(),
                domain.getEmail().getValue(),
                domain.getPassword().getValue(),
                domain.getRole(),
                domain.getPlan(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }

    public User toDomain(UserEntity entity) {

        return User.restore(
                entity.getId(),
                new Email(entity.getEmail()),
                new Password(entity.getPassword()),
                entity.getRole(),
                entity.getPlan(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public void updateEntityFromDomain(User domain, UserEntity entity) {

        entity.setPassword(domain.getPassword().getValue());
        entity.setRole(domain.getRole());
        entity.setPlan(domain.getPlan());
        entity.setUpdatedAt(domain.getUpdatedAt());
    }
}
