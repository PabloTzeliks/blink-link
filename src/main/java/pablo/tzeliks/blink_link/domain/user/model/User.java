package pablo.tzeliks.blink_link.domain.user.model;

import pablo.tzeliks.blink_link.domain.user.model.valueobject.Email;
import pablo.tzeliks.blink_link.domain.user.model.valueobject.Password;

import java.time.LocalDateTime;
import java.util.UUID;

public class User {

    private final UUID id;
    private Email email;
    private Password password;
    private Role role;
    private Plan plan;

    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
