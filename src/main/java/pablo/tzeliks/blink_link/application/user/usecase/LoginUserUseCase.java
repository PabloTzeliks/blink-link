package pablo.tzeliks.blink_link.application.user.usecase;

import org.springframework.stereotype.Service;
import pablo.tzeliks.blink_link.application.user.dto.LoginUserRequest;
import pablo.tzeliks.blink_link.application.user.dto.UserResponse;
import pablo.tzeliks.blink_link.domain.user.ports.TokenGenerationPort;

@Service
public class LoginUserUseCase {

    private TokenGenerationPort tokenPort;

    public UserResponse execute(LoginUserRequest request) {


    }
}
