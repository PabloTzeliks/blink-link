package pablo.tzeliks.blink_link.infrastructure.security.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import pablo.tzeliks.blink_link.domain.user.model.User;
import pablo.tzeliks.blink_link.infrastructure.security.adapter.CustomUserDetails;
import pablo.tzeliks.blink_link.infrastructure.security.jwt.TokenService;

import java.io.IOException;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final TokenService tokenService;

    @Value("${app.frontend.oauth2-redirect-url:http://localhost:3000/dashboard}")
    private String frontendRedirectUrl;

    public OAuth2LoginSuccessHandler(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

        User user = customUserDetails.getUser();

        String token = tokenService.generateToken(user);

        // 1. Criamos o Cookie Seguro
        Cookie jwtCookie = new Cookie("jwt_token", token);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(3600);

        response.addCookie(jwtCookie);

        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, frontendRedirectUrl);
    }
}
