package pablo.tzeliks.blink_link.domain.user.model;

import org.h2.schema.Domain;
import pablo.tzeliks.blink_link.domain.common.exception.DomainException;
import pablo.tzeliks.blink_link.domain.user.exception.InvalidPasswordException;
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


    private User(UUID id,
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

    public static User create(Email email, Password password) {
        return new User(UUID.randomUUID(), email, password, Role.USER, Plan.FREE, LocalDateTime.now(), LocalDateTime.now());
    }

    public static User restore(UUID id, Email email, Password password, Role role, Plan plan, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new User(id, email, password, role, plan, createdAt, updatedAt);
    }

    public void upgradeToVip() {
        this.plan = Plan.VIP;
    }

    public void promoteToAdmin() {
        this.role = Role.ADMIN;
    }

    public void changePassword(Password newPassword) {

        if (newPassword == this.password) {

            throw new InvalidPasswordException("Invalid Password.");
        }

        this.password = newPassword;
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
