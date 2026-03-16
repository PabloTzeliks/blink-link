package pablo.tzeliks.blink_link.infrastructure.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import pablo.tzeliks.blink_link.domain.user.model.User;
import pablo.tzeliks.blink_link.domain.user.model.valueobject.Email;
import pablo.tzeliks.blink_link.domain.user.ports.UserRepositoryPort;
import pablo.tzeliks.blink_link.infrastructure.security.adapter.CustomUserDetails;
import pablo.tzeliks.blink_link.infrastructure.security.jwt.TokenService;

import java.io.IOException;
import java.util.Optional;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UserRepositoryPort repositoryPort;

    public SecurityFilter(TokenService tokenService, UserRepositoryPort repositoryPort) {
        this.tokenService = tokenService;
        this.repositoryPort = repositoryPort;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        var token = this.recoverToken(request);

        if (token != null) {

            var emailString = tokenService.validateToken(token);

            if (!emailString.isEmpty()) {

                Email email = new Email(emailString);
                Optional<User> user = repositoryPort.findByEmail(email);

                if (user.isPresent()) {

                    CustomUserDetails customUserDetails = new CustomUserDetails(user.get());

                    var authentication = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private String recoverToken(HttpServletRequest request) {

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {

                if (cookie.getName().equals("jwt_token")) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
}
