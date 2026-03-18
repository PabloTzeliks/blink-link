package pablo.tzeliks.blink_link.domain.user.model;

import pablo.tzeliks.blink_link.domain.common.exception.AuthenticationException;
import pablo.tzeliks.blink_link.domain.user.exception.InvalidPasswordException;
import pablo.tzeliks.blink_link.domain.user.exception.OAuth2AuthenticationException;
import pablo.tzeliks.blink_link.domain.user.model.valueobject.Email;
import pablo.tzeliks.blink_link.domain.user.model.valueobject.Password;

import java.time.LocalDateTime;
import java.util.UUID;

public class User {

    private final UUID id;
    private final Email email;
    private Password password;
    private Role role;
    private Plan plan;
    private AuthProvider authProvider;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private User(UUID id,
                 Email email,
                 Password password,
                 Role role,
                 Plan plan,
                 AuthProvider authProvider,
                 LocalDateTime createdAt,
                 LocalDateTime updatedAt) {

        validatePasswordBasedOnProvider(password, authProvider);

        this.id = id;
        this.email = email;
        this.password = password;
        this.role = role;
        this.plan = plan;
        this.authProvider = authProvider;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static User createLocal(Email email, Password password) {
        return new User(UUID.randomUUID(), email, password, Role.USER, Plan.FREE, AuthProvider.LOCAL, LocalDateTime.now(), LocalDateTime.now());
    }

    public static User createOAuth2(Email email, AuthProvider authProvider) {
        if (authProvider.equals(AuthProvider.LOCAL)) {
            throw new OAuth2AuthenticationException("OAuth2 users must have a correct authentication provider.");
        }

        return new User(UUID.randomUUID(), email, null, Role.USER, Plan.FREE, authProvider, LocalDateTime.now(), LocalDateTime.now());
    }

    public static User restore(UUID id,
                               Email email,
                               Password password,
                               Role role, Plan plan,
                               AuthProvider authProvider,
                               LocalDateTime createdAt,
                               LocalDateTime updatedAt) {

        return new User(id, email, password, role, plan, authProvider, createdAt, updatedAt);
    }

    private void validatePasswordBasedOnProvider(Password password, AuthProvider provider) {
        if (provider == AuthProvider.LOCAL && password == null) {
            throw new AuthenticationException("Local users must have a Password.");
        }
    }

    public void changePlan(Plan plan) {

        if (plan == this.plan) {


        }

        this.updatedAt = LocalDateTime.now();
    }

    public void promoteToAdmin() {
        this.role = Role.ADMIN;
        this.updatedAt = LocalDateTime.now();
    }

    public void changePassword(Password newPassword) {

        if (this.authProvider != AuthProvider.LOCAL && this.password == null) {
            throw new InvalidPasswordException("Authenticated users with " + this.authProvider + " does not manage password here.");
        }

        if (newPassword.equals(this.password)) {
            throw new InvalidPasswordException("New password must be different from the current password.");
        }

        this.password = newPassword;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean hasPassword() {
        return this.password != null;
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

    public AuthProvider getAuthProvider() {
        return authProvider;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
