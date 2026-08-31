package io.github.jefersonsantos06.stdnum.algo;

import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.ValidationException;

/**
 * The Luhn algorithm (ISO/IEC 7812-1) and its mod N generalisation.
 *
 * <p>Walking the number right to left, every second value (starting with the
 * second-rightmost) is doubled and reduced by summing its "digits" in the
 * alphabet radix; a valid number has a total checksum of 0. Passing a custom
 * alphabet turns this into the Luhn mod N algorithm; the default alphabet is
 * {@code "0123456789"}.</p>
 *
 * <p>Like all algorithm classes, these methods take the number as-is: cleaning
 * separators is the caller's job.</p>
 */
public final class Luhn {

    /** The default, decimal alphabet. */
    public static final String DECIMAL_ALPHABET = "0123456789";

    private Luhn() {
    }

    /** Checksum over {@code number}; valid numbers have a checksum of 0. */
    public static int checksum(String number) {
        return checksum(number, DECIMAL_ALPHABET);
    }

    /**
     * Checksum over {@code number} using the given alphabet (Luhn mod N);
     * valid numbers have a checksum of 0.
     *
     * @throws InvalidFormatException on {@code null}, empty input or characters
     *                                outside the alphabet
     */
    public static int checksum(String number, String alphabet) {
        if (number == null || number.isEmpty()) {
            throw new InvalidFormatException();
        }
        int n = alphabet.length();
        int sum = 0;
        for (int i = number.length() - 1, pos = 0; i >= 0; i--, pos++) {
            int value = alphabet.indexOf(number.charAt(i));
            if (value < 0) {
                throw new InvalidFormatException();
            }
            if ((pos & 1) == 0) {
                sum += value;
            } else {
                int doubled = value * 2;
                sum += doubled / n + doubled % n;
            }
        }
        return sum % n;
    }

    /** The check digit to append to {@code number} to make it valid. */
    public static char calcCheckDigit(String number) {
        return calcCheckDigit(number, DECIMAL_ALPHABET);
    }

    /** The check digit to append to {@code number} to make it valid (mod N). */
    public static char calcCheckDigit(String number, String alphabet) {
        int n = alphabet.length();
        int checksum = checksum(number + alphabet.charAt(0), alphabet);
        return alphabet.charAt((n - checksum) % n);
    }

    /**
     * Checks that the number (including its final check digit) passes the
     * Luhn checksum and returns it unchanged.
     *
     * @throws InvalidChecksumException if the checksum does not match
     */
    public static String validate(String number) {
        return validate(number, DECIMAL_ALPHABET);
    }

    /** Mod N variant of {@link #validate(String)}. */
    public static String validate(String number, String alphabet) {
        if (checksum(number, alphabet) != 0) {
            throw new InvalidChecksumException();
        }
        return number;
    }

    /** Whether the number passes the Luhn checksum. Never throws. */
    public static boolean isValid(String number) {
        return isValid(number, DECIMAL_ALPHABET);
    }

    /** Mod N variant of {@link #isValid(String)}. */
    public static boolean isValid(String number, String alphabet) {
        try {
            validate(number, alphabet);
            return true;
        } catch (ValidationException e) {
            return false;
        }
    }
}
