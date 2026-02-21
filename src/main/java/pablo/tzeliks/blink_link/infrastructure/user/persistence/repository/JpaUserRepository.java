package pablo.tzeliks.blink_link.infrastructure.user.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pablo.tzeliks.blink_link.domain.user.model.User;

import java.util.UUID;

public interface JpaUserRepository extends JpaRepository<User, UUID> {
}
