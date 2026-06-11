package pablo.tzeliks.blink_link.infrastructure.persistence.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import pablo.tzeliks.blink_link.domain.url.model.Url;
import pablo.tzeliks.blink_link.domain.url.ports.UrlRepositoryPort;
import pablo.tzeliks.blink_link.domain.user.model.AuthProvider;
import pablo.tzeliks.blink_link.domain.user.model.Plan;
import pablo.tzeliks.blink_link.domain.user.model.Role;
import pablo.tzeliks.blink_link.infrastructure.AbstractContainerBase;
import pablo.tzeliks.blink_link.infrastructure.url.persistence.mapper.UrlEntityMapper;
import pablo.tzeliks.blink_link.infrastructure.url.persistence.repository.PostgresUrlRepositoryAdapter;
import pablo.tzeliks.blink_link.infrastructure.user.persistence.entity.UserEntity;
import pablo.tzeliks.blink_link.infrastructure.user.persistence.repository.JpaUserRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({PostgresUrlRepositoryAdapter.class, UrlEntityMapper.class})
public class PostgresUrlRepositoryIntegrationTest extends AbstractContainerBase {

    @Autowired
    private UrlRepositoryPort repository;

    @Autowired
    private JpaUserRepository jpaUserRepository;

    @Test
    @DisplayName("Should save an URL and find it by Short Code")
    void shouldSaveAnUrlAndFindItByShortCode() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UserEntity userEntity = new UserEntity(userId, "repo-test@example.com", "encoded",
                Role.USER, Plan.FREE, AuthProvider.LOCAL, LocalDateTime.now(), LocalDateTime.now());
        jpaUserRepository.saveAndFlush(userEntity);

        Long dummyId = Math.abs(new Random().nextLong());

        Url newUrl = Url.restore(
                dummyId, // Usa o ID inventado
                userId,
                "https://github.com/PabloTzeliks",
                "myGit",
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(7)
        );

        // Act
        repository.save(newUrl);

        // Assert
        Optional<Url> foundUrl = repository.findByShortCode("myGit");

        assertThat(foundUrl).isPresent();
        assertThat(foundUrl.get().getId()).isEqualTo(dummyId); // Verifica contra o ID inventado
        assertThat(foundUrl.get().getOriginalUrl()).isEqualTo("https://github.com/PabloTzeliks");
        assertThat(foundUrl.get().getShortCode()).isEqualTo("myGit");
        assertThat(foundUrl.get().getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should return empty when Short Code does not exist")
    void shouldReturnEmptyForNonExistentCode() {
        Optional<Url> foundUrl = repository.findByShortCode("ghost-code");
        assertThat(foundUrl).isEmpty();
    }
}