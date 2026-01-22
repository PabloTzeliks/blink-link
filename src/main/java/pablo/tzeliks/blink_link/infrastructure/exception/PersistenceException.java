package pablo.tzeliks.blink_link.infrastructure.exception;

public class PersistenceException extends RuntimeException {
    public PersistenceException(String message) {
        super(message);
    }
}
