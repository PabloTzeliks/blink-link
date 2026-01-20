package pablo.tzeliks.blink_link.logic;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class Base62Encoder implements ShortenLogic {

    @Value(value = "${blink-link.secret-key}")
    private String privateBase;

    @Override
    public String encode(Long id) {

        if (id == null) throw new IllegalArgumentException("ID cannot be Null");

        if (id == 0) return String.valueOf(privateBase.charAt(0));

        StringBuilder encoded = new StringBuilder();
        long base = privateBase.length();

        while (id > 0) {
            int remainder = (int) (id % base);
            encoded.append(privateBase.charAt((remainder)));

            id /= base;
        }

        return encoded.reverse().toString();
    }

    @Override
    public Long decode(String shortCode) {

        if (shortCode == null || shortCode.isEmpty()) throw new IllegalArgumentException("Short Code cannot be empty");

        long decoded = 0;
        long base = privateBase.length();

        for (char c : shortCode.toCharArray()) {
            int index = privateBase.indexOf(c);

            if (index == -1) throw new IllegalArgumentException("Character '" + c + "' not found in the Base");

            decoded = decoded * base + index;
        }

        return decoded;
    }
}
