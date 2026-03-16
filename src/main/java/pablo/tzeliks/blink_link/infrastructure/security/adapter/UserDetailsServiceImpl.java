package pablo.tzeliks.blink_link.infrastructure.security.adapter;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import pablo.tzeliks.blink_link.domain.user.model.User;
import pablo.tzeliks.blink_link.domain.user.model.valueobject.Email;
import pablo.tzeliks.blink_link.domain.user.ports.UserRepositoryPort;
import pablo.tzeliks.blink_link.infrastructure.user.persistence.repository.PostgresUserRepositoryAdapter;

public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepositoryPort repository;

    public UserDetailsServiceImpl(PostgresUserRepositoryAdapter repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Email email = new Email(username);

        User user = repository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));

        return new CustomUserDetails(user);
    }
}
