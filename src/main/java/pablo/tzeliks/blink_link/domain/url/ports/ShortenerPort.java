package pablo.tzeliks.blink_link.domain.url.ports;

/**
 *
 * @author Pablo Tzeliks
 * @version 2.0.0
 * @since 1.0.0
 */
public interface ShortenerPort {

    String encode(Long id);
}
