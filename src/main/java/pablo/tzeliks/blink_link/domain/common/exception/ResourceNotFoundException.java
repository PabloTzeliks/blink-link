package pablo.tzeliks.blink_link.domain.common.exception;

public class ResourceNotFoundException extends BusinessRuleException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
