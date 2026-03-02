package pablo.tzeliks.blink_link.infrastructure.security.adapter;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pablo.tzeliks.blink_link.infrastructure.user.persistence.entity.UserEntity;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private final UserEntity entity;

    public CustomUserDetails(UserEntity entity) {
        this.entity = entity;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + entity.getRole().name()));
    }

    @Override
    public @Nullable String getPassword() {
        return entity.getPassword();
    }

    @Override
    public String getUsername() {
        return entity.getEmail();
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
