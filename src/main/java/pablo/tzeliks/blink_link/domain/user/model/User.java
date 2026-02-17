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

    public User(UUID id,
                Email email,
                Password password,
                Role role, Plan plan,
                LocalDateTime createdAt,
                LocalDateTime updatedAt) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.role = role;
        this.plan = plan;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public User(Email email, Password password, Role role, Plan plan) {
        this.id = UUID.randomUUID();
        this.email = email;
        this.password = password;
        this.role = role;
        this.plan = plan;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }



    public UUID getId() {
        return id;
    }

    public Email getEmail() {
        return email;
    }

    public Password getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }

    public Plan getPlan() {
        return plan;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
