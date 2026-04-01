package pablo.tzeliks.blink_link.application.url.exception;

public class UrlCollisionException extends RuntimeException {
    public UrlCollisionException(String message, Throwable cause) {
        super(message, cause);
    }
}
