package io.github.jefersonsantos06.stdnum.spi;

import java.io.Serial;

/**
 * Thrown when the number's checksum or check digit does not match.
 */
public final class InvalidChecksumException extends ValidationException {

    @Serial
    private static final long serialVersionUID = 1L;

    public InvalidChecksumException() {
        this("The number's checksum or check digit is invalid.");
    }

    public InvalidChecksumException(String message) {
        super(ValidationError.INVALID_CHECKSUM, message);
    }

    /** With a message that can be translated; see {@link Message}. */
    public InvalidChecksumException(Message message) {
        super(ValidationError.INVALID_CHECKSUM, message);
    }
}
