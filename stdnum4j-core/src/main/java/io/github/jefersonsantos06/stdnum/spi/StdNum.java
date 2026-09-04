package io.github.jefersonsantos06.stdnum.spi;

import io.github.jefersonsantos06.stdnum.text.Mask;

import java.util.List;

/**
 * A standard number or code format: the common contract every number type in
 * this library implements.
 *
 * <p>The central design decision is that {@link #validate(String)}
 * <em>returns the compact representation</em>: validating and normalising are
 * the same call, so callers are never left holding an unnormalised string
 * after a successful validation.</p>
 *
 * <p>Implementations are immutable, stateless and thread-safe singletons,
 * conventionally exposed as a {@code public static final} field named
 * {@code INSTANCE} and registered with the registry through a
 * {@link StdNumProvider}.</p>
 *
 * <p>Robustness contract: for <em>any</em> input, including {@code null},
 * empty strings and garbage, the methods below either succeed or throw a
 * {@link ValidationException} subtype. They must never throw
 * {@code NullPointerException}, {@code IndexOutOfBoundsException} or any
 * other unchecked exception.</p>
 */
public interface StdNum {

    /** Metadata about this number type (id, names, country, tags). */
    Descriptor descriptor();

    /**
     * Converts the number to its minimal representation: strips valid
     * separators and surrounding whitespace, normalises letter case.
     * Does <em>not</em> check validity beyond basic input sanity.
     *
     * @throws ValidationException if the input is {@code null} or cannot be processed
     */
    String compact(String number);

    /**
     * Checks whether the number is valid and returns its compact
     * representation.
     *
     * @return the compact representation of the number
     * @throws InvalidFormatException    if characters or structure are not acceptable
     * @throws InvalidLengthException    if the length is wrong
     * @throws InvalidChecksumException  if the check digit(s) do not match
     * @throws InvalidComponentException if a part of the number is invalid or unknown
     */
    String validate(String number);

    /** Whether the number is valid. Never throws. */
    default boolean isValid(String number) {
        try {
            validate(number);
            return true;
        } catch (ValidationException e) {
            return false;
        }
    }

    /**
     * Exception-free validation for hot paths and for callers that want the
     * failure reason without a try/catch.
     */
    default Check check(String number) {
        try {
            return new Check.Valid(validate(number));
        } catch (ValidationException e) {
            return new Check.Invalid(e.error(), e.message());
        }
    }

    /**
     * Reformats the number to its standard presentation form
     * ({@code "390.533.447-05"}).
     *
     * <p>A presentation is the presentation <em>of a valid number</em>: this
     * validates first and throws the same {@link ValidationException} subtype
     * {@link #validate} would. python-stdnum instead regroups whatever it is
     * given, which hands back a well-dressed string for a number that is not
     * one — the point at which a number is formatted is usually the point at
     * which it goes onto an invoice or a screen, and that is the worst place
     * to launder it. A caller who wants the reference's behaviour writes
     * {@code try { format(x) } catch (ValidationException e) { showRaw(x); }}
     * and knows what it is showing; under the reference's rule there is no
     * signal to catch.</p>
     *
     * <p>The default implementation is the compact form, which is already the
     * presentation of a number written without separators. An implementation
     * that delegates to another type must pass its <em>own</em>
     * {@code validate} output — delegating the raw argument inherits the other
     * type's weaker rule, and a Belgian account number would come back
     * prettily grouped by a validator that rejects it.</p>
     *
     * @throws ValidationException if the number is not valid
     */
    default String format(String number) {
        return validate(number);
    }

    /**
     * How this number is written, as templates a form can put on an input
     * before there is a number to validate: {@code "###.###.###-##"} for the
     * CPF. Empty when the presentation is the compact form itself, and
     * {@link #format} agrees — a number no mask describes is its own
     * presentation.
     *
     * <p>A list, not one mask, because a type may write more than one shape:
     * Pernambuco writes nine digits as {@code 1908093-02} and the legacy
     * fourteen as {@code 18.1.001.0000004-9}, and a postal code takes the
     * shape its issuer's examples show. {@link Mask#apply(List, String)}
     * picks the one that fits a given compact number, which is what
     * {@code format} does with exactly this list.</p>
     *
     * <p>Empty does not promise the presentation is the compact form: a type
     * whose {@code format} is written by hand rather than through a mask —
     * the IBAN grouping in fours, the {@code CHE-} the Swiss UID writes in
     * front — has nothing to return here and still formats. Ask
     * {@code format} what a number looks like; ask this what an empty field
     * looks like.</p>
     *
     * @return the masks, outermost first, never {@code null}; an immutable list
     */
    default List<Mask> masks() {
        return List.of();
    }
}
