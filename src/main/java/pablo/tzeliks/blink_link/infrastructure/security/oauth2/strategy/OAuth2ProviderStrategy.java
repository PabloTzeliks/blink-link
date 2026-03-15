package pablo.tzeliks.blink_link.infrastructure.security.oauth2.strategy;

import java.util.Map;

public interface OAuth2ProviderStrategy {

    String getRegistrationId();
    String extractEmail(Map<String, Object> attributes);
}