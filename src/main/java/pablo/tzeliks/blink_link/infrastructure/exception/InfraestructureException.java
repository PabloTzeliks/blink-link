package pablo.tzeliks.blink_link.infrastructure.exception;

import pablo.tzeliks.blink_link.infrastructure.url.exception.EncoderException;

/**
 *
 * @author Pablo Tzeliks
 * @version 2.0.0
 * @since 1.0.0
 * @see EncoderException
 */
public class InfraestructureException extends RuntimeException {
    public InfraestructureException(String message) {
        super(message);
    }
}
