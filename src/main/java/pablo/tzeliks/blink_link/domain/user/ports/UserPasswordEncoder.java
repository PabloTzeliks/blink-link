package pablo.tzeliks.blink_link.domain.user.ports;

public interface UserPasswordEncoder {

    String encode(String rawPassword);
    boolean matches(String rawPassword, String encodedPassword);
}
