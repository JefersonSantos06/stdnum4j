package io.github.jefersonsantos06.stdnum.spi;

/**
 * The reason a number failed validation.
 *
 * <p>Every {@link ValidationException} carries exactly one of these values so
 * callers can branch on the failure reason without instanceof checks.</p>
 */
public enum ValidationError {

    /** The number contains characters or a structure that is not acceptable. */
    INVALID_FORMAT,

    /** The number has a wrong length (a specialisation of an invalid format). */
    INVALID_LENGTH,

    /** The checksum or check digit of the number does not match. */
    INVALID_CHECKSUM,

    /** A part of the number (country, region, bank, date, ...) is invalid or unknown. */
    INVALID_COMPONENT
}
