package pablo.tzeliks.blink_link.infrastructure.security.oauth2.strategy;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GoogleOAuth2Strategy implements OAuth2ProviderStrategy {

    @Override
    public String getRegistrationId() { return "google"; }

    @Override
    public String extractEmail(Map<String, Object> attributes) {
        return (String) attributes.get("email");
    }
}
