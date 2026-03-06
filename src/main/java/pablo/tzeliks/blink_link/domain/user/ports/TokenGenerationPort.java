package pablo.tzeliks.blink_link.domain.user.ports;

import pablo.tzeliks.blink_link.domain.user.model.User;

public interface TokenGenerationPort {

    String generateToken(User user);
}
