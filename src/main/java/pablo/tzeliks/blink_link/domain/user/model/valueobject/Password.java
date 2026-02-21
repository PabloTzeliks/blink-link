package pablo.tzeliks.blink_link.domain.user.model.valueobject;

public record Password(String value) {

    public Password {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
    }

    public String getValue() {
        return value;
    }
}
