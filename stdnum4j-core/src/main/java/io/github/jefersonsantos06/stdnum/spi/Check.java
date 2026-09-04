package io.github.jefersonsantos06.stdnum.spi;

import java.util.Objects;

/**
 * Exception-free validation result: either {@link Valid} with the compact
 * representation, or {@link Invalid} with the failure reason.
 *
 * <p>Designed for pattern matching:</p>
 *
 * <pre>{@code
 * switch (cpf.check(input)) {
 *     case Check.Valid v   -> store(v.compact());
 *     case Check.Invalid i -> reject(i.error(), i.reason());
 * }
 * }</pre>
 *
 * <p>{@link Invalid#reason()} is English, for a log. To show the rejection to
 * someone, pass the {@link Invalid} to a {@link Messages} for their
 * language.</p>
 */
public sealed interface Check {

    /** The number is valid; {@code compact} is the canonical minimal form. */
    record Valid(String compact) implements Check {
        public Valid {
            Objects.requireNonNull(compact, "compact");
        }
    }

    /**
     * The number is invalid for the given {@code error}, detailed by
     * {@code message} — which carries the reason without committing to a
     * language.
     */
    record Invalid(ValidationError error, Message message) implements Check {
        public Invalid {
            Objects.requireNonNull(error, "error");
            Objects.requireNonNull(message, "message");
        }

        /** A rejection whose reason is a plain English sentence. */
        public Invalid(ValidationError error, String reason) {
            this(error, Message.plain(reason));
        }

        /** The reason in English, which is what belongs in a log. */
        public String reason() {
            return message.text();
        }
    }

    /** Convenience shortcut for {@code this instanceof Valid}. */
    default boolean isValid() {
        return this instanceof Valid;
    }
}
