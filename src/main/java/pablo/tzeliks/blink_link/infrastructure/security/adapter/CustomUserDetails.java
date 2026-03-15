package pablo.tzeliks.blink_link.infrastructure.security.adapter;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import pablo.tzeliks.blink_link.domain.user.model.Plan;
import pablo.tzeliks.blink_link.domain.user.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CustomUserDetails implements UserDetails, OAuth2User {

    private final User domainUser;
    private final Map<String, Object> attributes;

    public CustomUserDetails(User domainUser) {
        this.domainUser = domainUser;
        this.attributes = null;
    }

    public CustomUserDetails(User domainUser, Map<String, Object> attributes) {
        this.domainUser = domainUser;
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return domainUser.getEmail().getValue();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + domainUser.getRole().name()));
    }

    @Override
    public @Nullable String getPassword() {
        return domainUser.hasPassword() ? domainUser.getPassword().getValue() : null;
    }

    @Override
    public String getUsername() {
        return domainUser.getEmail().getValue();
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
    public Plan getPlan() { return domainUser.getPlan(); }
}
