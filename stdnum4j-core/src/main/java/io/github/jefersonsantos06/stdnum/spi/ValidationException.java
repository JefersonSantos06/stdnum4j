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
 *
 * <p>{@link #getMessage()} is the English sentence, which is what belongs in
 * a log and a stack trace. To say the same failure in another language, hand
 * {@link #message()} — or the exception itself — to a {@link Messages}.</p>
 */
public class ValidationException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final ValidationError error;

    /**
     * Only set when the failure named itself. It is not serialized either —
     * it holds a {@link Class}, which need not resolve where the exception is
     * read back — and {@link #message()} rebuilds a plain one from the text,
     * which is serialized with the exception as usual.
     */
    private final transient Message message;

    protected ValidationException(ValidationError error, String message) {
        // no Message is built here: three quarters of all failures take this
        // constructor, carry no code and can never be translated by one, and
        // this is the path a batch of millions of records runs down
        super(message, null, false, false);
        this.error = Objects.requireNonNull(error, "error");
        this.message = null;
    }

    protected ValidationException(ValidationError error, Message message) {
        // no cause, no suppression, no writable stack trace: cheap by design
        super(Objects.requireNonNull(message, "message").text(), null, false, false);
        this.error = Objects.requireNonNull(error, "error");
        this.message = message;
    }

    /** The machine-readable reason for this failure. */
    public ValidationError error() {
        return error;
    }

    /**
     * What the validator had to say, before a language was chosen. Pass it to
     * {@link Messages#render(ValidationError, Message)} to say it in one.
     */
    public Message message() {
        return message != null ? message : Message.plain(getMessage());
    }

    /**
     * {@inheritDoc}
     *
     * <p>The English sentence, which is what belongs in a log and a stack
     * trace. It is exactly what it was before this exception could be
     * translated at all.</p>
     */
    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
