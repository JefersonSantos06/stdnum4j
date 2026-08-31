package io.github.jefersonsantos06.stdnum.spi;

import java.io.Serial;

/**
 * Thrown when one of the parts of the number is invalid or unknown
 * (an unknown country prefix, a non-existent bank code, an impossible
 * birth date embedded in the number, ...).
 */
public final class InvalidComponentException extends ValidationException {

    @Serial
    private static final long serialVersionUID = 1L;

    public InvalidComponentException() {
        this("One of the parts of the number are invalid or unknown.");
    }

    public InvalidComponentException(String message) {
        super(ValidationError.INVALID_COMPONENT, message);
    }
}
