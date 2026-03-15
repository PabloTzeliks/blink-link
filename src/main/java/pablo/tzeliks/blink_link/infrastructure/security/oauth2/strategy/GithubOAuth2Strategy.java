package pablo.tzeliks.blink_link.infrastructure.security.oauth2.strategy;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GithubOAuth2Strategy implements OAuth2ProviderStrategy {

    @Override
    public String getRegistrationId() { return "github"; }

    @Override
    public String extractEmail(Map<String, Object> attributes) {
        String email = (String) attributes.get("email");
        if (email == null) {
            String login = (String) attributes.get("login");
            return login + "@users.noreply.github.com";
        }
        return email;
    }
}
