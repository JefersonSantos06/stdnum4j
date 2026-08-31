package io.github.jefersonsantos06.stdnum.algo;

import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.ValidationException;

/**
 * The ISO/IEC 7064 MOD 97-10 algorithm (two check digits), used by IBAN and
 * the ISO 11649 structured creditor reference.
 *
 * <p>Letters are expanded to their base-36 value (A=10 ... Z=35, case
 * insensitive) and the resulting decimal string is evaluated modulo 97. The
 * evaluation is streamed digit by digit, so numbers of any length are
 * processed without big-integer arithmetic. A valid number yields 1.</p>
 */
public final class Mod97 {

    private Mod97() {
    }

    /**
     * The number evaluated modulo 97 after base-36 expansion; valid numbers
     * (including their two check digits) yield 1.
     *
     * @throws InvalidFormatException on {@code null}, empty input or
     *                                non-alphanumeric characters
     */
    public static int checksum(String number) {
        if (number == null || number.isEmpty()) {
            throw new InvalidFormatException();
        }
        long acc = 0;
        for (int i = 0; i < number.length(); i++) {
            int value = Character.digit(number.charAt(i), 36);
            if (value < 0) {
                throw new InvalidFormatException();
            }
            acc = (value < 10 ? acc * 10 + value : acc * 100 + value) % 97;
        }
        return (int) acc;
    }

    /**
     * The two check digits that make {@code number + digits} valid, as a
     * two-character string (with leading zero when needed).
     */
    public static String calcCheckDigits(String number) {
        int digits = 98 - checksum(number + "00");
        return digits < 10 ? "0" + digits : Integer.toString(digits);
    }

    /**
     * Checks that the number (including its check digits) evaluates to 1 and
     * returns it unchanged.
     *
     * @throws InvalidChecksumException if the checksum does not match
     */
    public static String validate(String number) {
        if (checksum(number) != 1) {
            throw new InvalidChecksumException();
        }
        return number;
    }

    /** Whether the number passes the MOD 97-10 check. Never throws. */
    public static boolean isValid(String number) {
        try {
            validate(number);
            return true;
        } catch (ValidationException e) {
            return false;
        }
    }
}
