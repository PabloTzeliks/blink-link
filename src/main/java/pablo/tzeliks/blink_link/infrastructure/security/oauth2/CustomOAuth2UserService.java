package pablo.tzeliks.blink_link.infrastructure.security.oauth2;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import pablo.tzeliks.blink_link.application.user.usecase.ProcessOAuth2UserUseCase;
import pablo.tzeliks.blink_link.domain.user.exception.OAuth2AuthenticationException;
import pablo.tzeliks.blink_link.domain.user.model.AuthProvider;
import pablo.tzeliks.blink_link.domain.user.model.User;
import pablo.tzeliks.blink_link.domain.user.model.valueobject.Email;
import pablo.tzeliks.blink_link.infrastructure.security.adapter.CustomUserDetails;
import pablo.tzeliks.blink_link.infrastructure.security.oauth2.strategy.OAuth2ProviderStrategy;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final ProcessOAuth2UserUseCase processUserUseCase;
    private final Map<String, OAuth2ProviderStrategy> strategies;

    public CustomOAuth2UserService(ProcessOAuth2UserUseCase processUserUseCase,
                                   List<OAuth2ProviderStrategy> strategyList) {

        this.processUserUseCase = processUserUseCase;
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(OAuth2ProviderStrategy::getRegistrationId, s -> s));
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        AuthProvider provider = AuthProvider.valueOf(registrationId.toUpperCase());
        Map<String, Object> attributes = oAuth2User.getAttributes();

        OAuth2ProviderStrategy strategy = strategies.get(registrationId);

        if (strategy == null) {
            throw new OAuth2AuthenticationException("OAuth2 provider not supported: " + registrationId);
        }

        String emailStr = strategy.extractEmail(attributes);

        if (emailStr == null || emailStr.isBlank()) {
            throw new OAuth2AuthenticationException("E-mail not found for Provider " + provider);
        }

        User user = processUserUseCase.execute(new Email(emailStr), provider);

        return new CustomUserDetails(user, attributes);
    }
}
