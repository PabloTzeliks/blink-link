package pablo.tzeliks.blink_link.infrastructure.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;
import pablo.tzeliks.blink_link.domain.user.ports.UserRepositoryPort;
import pablo.tzeliks.blink_link.infrastructure.security.jwt.TokenService;

import java.io.IOException;

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
    }

    private String recoverToken(HttpServletRequest request) {

        var authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {

            return null;
        }

        return authHeader.replace("Bearer ", "");
    }
}
