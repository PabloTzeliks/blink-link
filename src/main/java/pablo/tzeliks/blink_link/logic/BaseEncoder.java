package pablo.tzeliks.blink_link.logic;

import org.springframework.stereotype.Component;

@Component
public class BaseEncoder {

    private static final String PRIVATE_BASE = "wJ9h4k2M0qL5O1vR8dG7sN4hV6cT3qXpYajeoyrmuniBWZKQAxvIHglUOPzfDtSC";

    public String encode(long id) {

        StringBuilder encoded = new StringBuilder();

        while (id > 0) {

            long remainder = id % PRIVATE_BASE.length();
            encoded.append(PRIVATE_BASE.charAt((int) remainder));

            id /= PRIVATE_BASE.length();
        }

        return encoded.reverse().toString();
    }

    public long decode(String urlEncoded) {

        long decoded = 0;

        for (int i = 0; i < urlEncoded.length(); i++) {

            int index = PRIVATE_BASE.indexOf(urlEncoded.charAt(i));
            decoded = decoded * PRIVATE_BASE.length() + index;
        }

        return decoded;
    }
}
