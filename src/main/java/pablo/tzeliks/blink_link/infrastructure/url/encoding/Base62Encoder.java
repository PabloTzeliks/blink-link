package pablo.tzeliks.blink_link.infrastructure.url.encoding;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import pablo.tzeliks.blink_link.domain.url.ports.ShortenerPort;
import pablo.tzeliks.blink_link.infrastructure.url.exception.EncoderException;

/**
 *
 * @author Pablo Tzeliks
 * @version 2.0.0
 * @since 1.0.0
 * @see ShortenerPort
 */
@Component
@Primary
public class Base62Encoder implements ShortenerPort {

    @Value(value = "${blink-link.secret-key}")
    private String privateBase;

    private int base;

    @PostConstruct
    public void init() {
        this.base = privateBase.length();
    }

    @Override
    public String encode(Long id) {

        if (id == null) { throw new EncoderException("ID cannot be null"); }

        if (id < 0) { throw new EncoderException("ID cannot be negative"); }

        if (id == 0) { return String.valueOf(privateBase.charAt(0)); }

        StringBuilder encoded = new StringBuilder();

        while (id > 0) {
            int remainder = (int) (id % base);
            encoded.append(privateBase.charAt(remainder));
            id /= base;
        }

        return encoded.reverse().toString();
    }
}