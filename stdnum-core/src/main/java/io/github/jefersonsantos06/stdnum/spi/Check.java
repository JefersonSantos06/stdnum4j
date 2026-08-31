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
 */
public sealed interface Check {

    /** The number is valid; {@code compact} is the canonical minimal form. */
    record Valid(String compact) implements Check {
        public Valid {
            Objects.requireNonNull(compact, "compact");
        }
    }

    /** The number is invalid for the given {@code error}, detailed by {@code reason}. */
    record Invalid(ValidationError error, String reason) implements Check {
        public Invalid {
            Objects.requireNonNull(error, "error");
            if (reason == null) {
                reason = "";
            }
        }
    }

    /** Convenience shortcut for {@code this instanceof Valid}. */
    default boolean isValid() {
        return this instanceof Valid;
    }
}
