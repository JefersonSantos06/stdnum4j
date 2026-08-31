package io.github.jefersonsantos06.stdnum.spi;

import java.io.Serial;

/**
 * Thrown when the number has a wrong length.
 *
 * <p>Extends {@link InvalidFormatException} because a wrong length is a kind
 * of format problem: code that catches format errors also catches length
 * errors, mirroring the hierarchy callers usually want.</p>
 */
public final class InvalidLengthException extends InvalidFormatException {

    @Serial
    private static final long serialVersionUID = 1L;

    public InvalidLengthException() {
        this("The number has an invalid length.");
    }

    public InvalidLengthException(String message) {
        super(ValidationError.INVALID_LENGTH, message);
    }
}
