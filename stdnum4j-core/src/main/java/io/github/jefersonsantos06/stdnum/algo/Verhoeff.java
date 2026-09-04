package io.github.jefersonsantos06.stdnum.algo;

import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.ValidationException;

/**
 * The Verhoeff check digit algorithm.
 *
 * <p>Uses the dihedral group D5: a multiplication table {@code D}, a
 * permutation table {@code P} applied by digit position (mod 8, right to
 * left), and the group inverse to derive check digits. Detects all
 * single-digit errors and all adjacent transpositions.</p>
 */
public final class Verhoeff {

    private Verhoeff() {
    }

    /** Multiplication in the dihedral group D5. */
    private static final int[][] D = {
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9},
            {1, 2, 3, 4, 0, 6, 7, 8, 9, 5},
            {2, 3, 4, 0, 1, 7, 8, 9, 5, 6},
            {3, 4, 0, 1, 2, 8, 9, 5, 6, 7},
            {4, 0, 1, 2, 3, 9, 5, 6, 7, 8},
            {5, 9, 8, 7, 6, 0, 4, 3, 2, 1},
            {6, 5, 9, 8, 7, 1, 0, 4, 3, 2},
            {7, 6, 5, 9, 8, 2, 1, 0, 4, 3},
            {8, 7, 6, 5, 9, 3, 2, 1, 0, 4},
            {9, 8, 7, 6, 5, 4, 3, 2, 1, 0},
    };

    /** Position-dependent permutation, applied as {@code P[position % 8]}. */
    private static final int[][] P = {
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9},
            {1, 5, 7, 6, 2, 8, 3, 0, 9, 4},
            {5, 8, 0, 3, 7, 9, 6, 1, 4, 2},
            {8, 9, 1, 6, 0, 4, 3, 5, 2, 7},
            {9, 4, 5, 3, 1, 2, 6, 8, 7, 0},
            {4, 2, 8, 6, 5, 7, 3, 9, 0, 1},
            {2, 7, 9, 3, 8, 0, 6, 4, 1, 5},
            {7, 0, 4, 6, 9, 1, 3, 2, 5, 8},
    };

    /** Inverse in D5: {@code D[c][INV[c]] == 0}. */
    private static final int[] INV = {0, 4, 3, 2, 1, 5, 6, 7, 8, 9};

    /**
     * The Verhoeff checksum; valid numbers (including their check digit)
     * yield 0.
     *
     * @throws InvalidFormatException on {@code null}, empty or non-digit input
     */
    public static int checksum(String number) {
        if (number == null || number.isEmpty()) {
            throw new InvalidFormatException();
        }
        int check = 0;
        for (int i = number.length() - 1, pos = 0; i >= 0; i--, pos++) {
            char c = number.charAt(i);
            if (c < '0' || c > '9') {
                throw new InvalidFormatException();
            }
            check = D[check][P[pos % 8][c - '0']];
        }
        return check;
    }

    /** The check digit to append to {@code number} to make it valid. */
    public static char calcCheckDigit(String number) {
        return (char) ('0' + INV[checksum(number + "0")]);
    }

    /**
     * Checks that the number (including its final check digit) yields
     * checksum 0 and returns it unchanged.
     *
     * @throws InvalidChecksumException if the checksum does not match
     */
    public static String validate(String number) {
        if (checksum(number) != 0) {
            throw new InvalidChecksumException();
        }
        return number;
    }

    /** Whether the number passes the Verhoeff check. Never throws. */
    public static boolean isValid(String number) {
        try {
            validate(number);
            return true;
        } catch (ValidationException e) {
            return false;
        }
    }
}
