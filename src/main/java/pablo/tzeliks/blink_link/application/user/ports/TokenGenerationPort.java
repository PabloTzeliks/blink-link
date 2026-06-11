package pablo.tzeliks.blink_link.application.user.ports;

import pablo.tzeliks.blink_link.domain.user.model.User;

public interface TokenGenerationPort {

    String generateToken(User user);
}
