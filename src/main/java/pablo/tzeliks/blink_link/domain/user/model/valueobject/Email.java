package pablo.tzeliks.blink_link.domain.user.model.valueobject;

import pablo.tzeliks.blink_link.domain.user.exception.InvalidEmailException;

import java.util.regex.Pattern;

public record Email(String value) {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");

    public Email {
        if (value == null || !EMAIL_PATTERN.matcher(value).matches()) {
            throw new InvalidEmailException("Invalid email format.");
        }
    }
}
