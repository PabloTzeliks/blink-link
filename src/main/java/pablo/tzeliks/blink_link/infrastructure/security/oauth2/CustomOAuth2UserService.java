package pablo.tzeliks.blink_link.infrastructure.security.oauth2;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.stereotype.Service;
import pablo.tzeliks.blink_link.domain.user.ports.UserRepositoryPort;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepositoryPort userRepository;

    public CustomOAuth2UserService(UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }


}
