package io.github.jefersonsantos06.stdnum.spi;

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
            return new Check.Invalid(e.error(), e.getMessage());
        }
    }

    /**
     * Reformats the number to its standard presentation form
     * ({@code "390.533.447-05"}). The default implementation returns the
     * compact representation.
     */
    default String format(String number) {
        return compact(number);
    }
}
