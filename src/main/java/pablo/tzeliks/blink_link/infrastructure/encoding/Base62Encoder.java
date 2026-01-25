package pablo.tzeliks.blink_link.infrastructure.encoding;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import pablo.tzeliks.blink_link.domain.url.ports.ShortenerPort;
import pablo.tzeliks.blink_link.infrastructure.exception.EncoderException;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO : new Docs
 *
 * @author Pablo Tzeliks
 * @version 2.0.0
 * @since 1.0.0
 */
@Component
@Primary
public class Base62Encoder implements ShortenerPort {

    @Value(value = "${blink-link.secret-key}")
    private String privateBase;

    private Map<Character, Integer> characterIndexMap;
    private int base;

    @PostConstruct
    public void init() {
        this.characterIndexMap = new HashMap<>();
        this.base = privateBase.length();
        char[] chars = privateBase.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            characterIndexMap.put(chars[i], i);
        }
    }

    @Override
    public String encode(Long id) {
        if (id == null) throw new EncoderException("ID cannot be null");
        if (id == 0) return String.valueOf(privateBase.charAt(0));

        StringBuilder encoded = new StringBuilder();

        while (id > 0) {
            int remainder = (int) (id % base);
            encoded.append(privateBase.charAt(remainder));
            id /= base;
        }

        return encoded.reverse().toString();
    }

    @Override
    public Long decode(String shortCode) {
        if (shortCode == null || shortCode.isEmpty()) {
            throw new EncoderException("Short Code cannot be empty");
        }

        long decoded = 0;

        for (char c : shortCode.toCharArray()) {
            Integer index = characterIndexMap.get(c);

            if (index == null) {
                throw new EncoderException("Character '" + c + "' is invalid for Base62 alphabet.");
            }

            decoded = decoded * base + index;
        }
        return decoded;
    }
}
