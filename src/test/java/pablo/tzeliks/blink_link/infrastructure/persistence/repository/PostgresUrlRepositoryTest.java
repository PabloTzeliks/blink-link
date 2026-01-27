package pablo.tzeliks.blink_link.infrastructure.persistence.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import pablo.tzeliks.blink_link.domain.url.model.Url;
import pablo.tzeliks.blink_link.domain.url.ports.UrlRepositoryPort;
import pablo.tzeliks.blink_link.infrastructure.persistence.AbstractContainerBase;
import pablo.tzeliks.blink_link.infrastructure.persistence.mapper.UrlEntityMapper;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({PostgresUrlRepository.class, UrlEntityMapper.class})
public class PostgresUrlRepositoryTest extends AbstractContainerBase {

    @Autowired
    private UrlRepositoryPort repository;

    @Test
    @DisplayName("Should retrieve the next ID from database sequence")
    void shouldGetNextId() {
        // Act
        Long firstId = repository.nextId();
        Long secondId = repository.nextId();

        Long testId = firstId + 1;

        // Assert
        assertThat(firstId).isNotNull();
        assertThat(secondId).isNotNull();
        assertThat(secondId).isGreaterThan(firstId);
        assertThat(testId).isEqualTo(secondId);
    }

    @Test
    @DisplayName("Should save an URL and find it by Short Code")
    void shouldSaveAnUrlAndFindItByShortCode() {
        // Arrange

        // 1. Generate new ID
        Long generatedId = repository.nextId();

        // 2. Create new Url
        Url newUrl = new Url(
                generatedId,
                "https://github.com/PabloTzeliks",
                "myGitHub",
                LocalDateTime.now()
        );

        // Act
        repository.save(newUrl);

        // Assert
        Optional<Url> foundUrl = repository.findByShortCode("myGitHub");

        assertThat(foundUrl).isPresent();
        assertThat(foundUrl.get().getId()).isEqualTo(generatedId);
        assertThat(foundUrl.get().getOriginalUrl()).isEqualTo("https://github.com/PabloTzeliks");
        assertThat(foundUrl.get().getShortCode()).isEqualTo("myGitHub");
        assertThat(foundUrl.get().getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should return empty when Short Code does not exist")
    void shouldReturnEmptyForNonExistentCode() {
        Optional<Url> foundUrl = repository.findByShortCode("ghost-code");
        assertThat(foundUrl).isEmpty();
    }
}
