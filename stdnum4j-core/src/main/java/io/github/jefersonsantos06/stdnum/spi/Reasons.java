package io.github.jefersonsantos06.stdnum.spi;

/**
 * The reasons more than one kind of number gives.
 *
 * <p>A birth date that is not a date is the same fact whether the number is
 * Belgian, Brazilian, Korean or South African, and it was written out
 * twenty times before this existed. Here it is written once, so it is
 * translated once and reads the same in every language.</p>
 *
 * <p>Each is an immutable constant anchored in this package, so a validator
 * that uses one costs no allocation and needs no translation file of its
 * own. A reason that only one number can give does not belong here: it goes
 * in that number's own package, through
 * {@link Message#of(Class, String, String, Object...)}.</p>
 */
public final class Reasons {

    private static final Message BIRTH_DATE = shared(
            "date.birth", "The number does not contain a valid birth date.");
    private static final Message NO_CHECK_DIGIT = shared(
            "check-digit.none", "No valid check digit exists for this number.");
    private static final Message NULL_NUMBER = shared(
            "number.null", "The number is null.");
    private static final Message NULL_TEXT = shared(
            "text.null", "The text is null.");
    private static final Message UNKNOWN_PROVINCE = shared(
            "province.unknown", "Unknown province code.");
    private static final Message PROVINCE_CODE = shared(
            "province.code", "Not the code of a province.");
    private static final Message ZERO_SEQUENCE = shared(
            "sequence.zero", "The sequence number must not be zero.");

    private Reasons() {
    }

    private static Message shared(String code, String english) {
        return Message.of(ValidationError.class, code, english);
    }

    /** The digits where a date of birth should be do not name a day. */
    public static Message birthDate() {
        return BIRTH_DATE;
    }

    /** No check digit could close this number, whatever it were. */
    public static Message noCheckDigit() {
        return NO_CHECK_DIGIT;
    }

    /** There is no number: the caller passed {@code null}. */
    public static Message nullNumber() {
        return NULL_NUMBER;
    }

    /** There is no text: the caller passed {@code null}. */
    public static Message nullText() {
        return NULL_TEXT;
    }

    /** The number names a province, and no province has that code. */
    public static Message unknownProvince() {
        return UNKNOWN_PROVINCE;
    }

    /** Where the number should carry a province code, it carries something else. */
    public static Message provinceCode() {
        return PROVINCE_CODE;
    }

    /** The serial part of the number is all zeroes, which is never issued. */
    public static Message zeroSequence() {
        return ZERO_SEQUENCE;
    }
}
