package io.github.jefersonsantos06.stdnum.spi;

import java.io.Serial;
import java.util.Objects;

/**
 * Base exception for all number validation failures.
 *
 * <p>This exception is deliberately cheap: it carries <em>no stack trace</em>
 * and no suppression, because {@link StdNum#validate(String)} uses it as a
 * signalling mechanism on a potentially hot path (think batch validation of
 * millions of records). Callers that do not want exceptions at all should use
 * {@link StdNum#check(String)} or {@link StdNum#isValid(String)}.</p>
 *
 * <p>It is unchecked so that {@code validate()} composes cleanly with streams
 * and lambdas. Only the concrete subtypes can be instantiated; catch this type
 * to handle any validation failure.</p>
 */
public class ValidationException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final ValidationError error;

    protected ValidationException(ValidationError error, String message) {
        // no cause, no suppression, no writable stack trace: cheap by design
        super(message, null, false, false);
        this.error = Objects.requireNonNull(error, "error");
    }

    /** The machine-readable reason for this failure. */
    public ValidationError error() {
        return error;
    }
}
