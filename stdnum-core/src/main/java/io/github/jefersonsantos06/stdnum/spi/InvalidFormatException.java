package io.github.jefersonsantos06.stdnum.spi;

import java.io.Serial;

/**
 * Thrown when the number's characters or structure are not acceptable
 * (for example letters where only digits are allowed).
 */
public class InvalidFormatException extends ValidationException {

    @Serial
    private static final long serialVersionUID = 1L;

    public InvalidFormatException() {
        this("The number has an invalid format.");
    }

    public InvalidFormatException(String message) {
        super(ValidationError.INVALID_FORMAT, message);
    }

    /** For subclasses that refine the error value (see {@link InvalidLengthException}). */
    protected InvalidFormatException(ValidationError error, String message) {
        super(error, message);
    }
}
