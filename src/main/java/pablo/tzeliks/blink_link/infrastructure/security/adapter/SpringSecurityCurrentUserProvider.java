package pablo.tzeliks.blink_link.infrastructure.security.adapter;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import pablo.tzeliks.blink_link.domain.user.ports.CurrentUserProviderPort;
import pablo.tzeliks.blink_link.domain.user.model.Plan;
import pablo.tzeliks.blink_link.infrastructure.exception.InfraestructureException;

@Component
public class SpringSecurityCurrentUserProvider implements CurrentUserProviderPort {

    @Override
    public Plan getCurrentUserPlan() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AuthenticationException("User not logged in.") {
            };
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails.getPlan();
        }

        throw new InfraestructureException("Cannot get current user plan.");
    }
}
