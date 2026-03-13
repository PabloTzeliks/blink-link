package pablo.tzeliks.blink_link.domain.user.ports;

public interface UserPasswordEncoderPort {

    String encode(String rawPassword);
    boolean matches(String rawPassword, String encodedPassword);
}
