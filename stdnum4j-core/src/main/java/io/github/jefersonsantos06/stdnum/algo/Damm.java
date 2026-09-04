package io.github.jefersonsantos06.stdnum.algo;

import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.ValidationException;

/**
 * The Damm check digit algorithm.
 *
 * <p>Based on a totally anti-symmetric quasigroup of order 10, it detects all
 * single-digit errors and all adjacent transposition errors. The operation
 * table below is the standard published one; because its diagonal is zero,
 * the interim value itself is the check digit and a valid number folds to 0.</p>
 */
public final class Damm {

    private Damm() {
    }

    /** The standard order-10 totally anti-symmetric quasigroup operation table. */
    private static final int[][] TABLE = {
            {0, 3, 1, 7, 5, 9, 8, 6, 4, 2},
            {7, 0, 9, 2, 1, 5, 4, 8, 6, 3},
            {4, 2, 0, 6, 8, 7, 1, 3, 5, 9},
            {1, 7, 5, 0, 9, 8, 3, 4, 2, 6},
            {6, 1, 2, 3, 0, 4, 5, 9, 7, 8},
            {3, 6, 7, 4, 2, 0, 9, 5, 8, 1},
            {5, 8, 6, 9, 7, 2, 0, 1, 3, 4},
            {8, 9, 4, 5, 3, 6, 2, 0, 1, 7},
            {9, 4, 3, 8, 6, 1, 7, 2, 0, 5},
            {2, 5, 8, 1, 4, 3, 6, 7, 9, 0},
    };

    /**
     * Folds the number through the operation table; valid numbers (including
     * their check digit) yield 0.
     *
     * @throws InvalidFormatException on {@code null}, empty or non-digit input
     */
    public static int checksum(String number) {
        if (number == null || number.isEmpty()) {
            throw new InvalidFormatException();
        }
        int interim = 0;
        for (int i = 0; i < number.length(); i++) {
            char c = number.charAt(i);
            if (c < '0' || c > '9') {
                throw new InvalidFormatException();
            }
            interim = TABLE[interim][c - '0'];
        }
        return interim;
    }

    /** The check digit to append to {@code number} to make it valid. */
    public static char calcCheckDigit(String number) {
        return (char) ('0' + checksum(number));
    }

    /**
     * Checks that the number (including its final check digit) folds to 0 and
     * returns it unchanged.
     *
     * @throws InvalidChecksumException if the checksum does not match
     */
    public static String validate(String number) {
        if (checksum(number) != 0) {
            throw new InvalidChecksumException();
        }
        return number;
    }

    /** Whether the number passes the Damm check. Never throws. */
    public static boolean isValid(String number) {
        try {
            validate(number);
            return true;
        } catch (ValidationException e) {
            return false;
        }
    }
}
