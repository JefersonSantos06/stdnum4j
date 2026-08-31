package io.github.jefersonsantos06.stdnum.algo;

import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;

/**
 * Weighted sum check digits, the workhorse behind most Brazilian numbers
 * (CPF, CNPJ, PIS/PASEP, RENAVAM, NF-e access keys, CNS, ...) and many
 * other national schemes: multiply each position by a weight, sum, and derive
 * the check digit modulo 11.
 *
 * <p>Character values are taken as {@code ASCII - 48}: digits {@code '0'-'9'}
 * map to 0-9 and letters {@code 'A'-'Z'} map to 17-42. The letter rule is
 * exactly the one adopted for the alphanumeric CNPJ (valid from July 2026),
 * which makes the same code handle numeric and alphanumeric identifiers.</p>
 *
 * <p>The two weight families cover the common national schemes:</p>
 * <ul>
 *   <li>{@link #descending(int, int)}: {@code descending(10, 9)} gives
 *       {@code [10,9,...,2]} (CPF first digit).</li>
 *   <li>{@link #cyclic(int, int...)}: repeats a cycle from the <em>rightmost</em>
 *       position leftwards; {@code cyclic(12, 2,3,4,5,6,7,8,9)} gives
 *       {@code [5,4,3,2,9,8,7,6,5,4,3,2]} (CNPJ first digit).</li>
 * </ul>
 *
 * <p>A note on the modulo: the textbook formula {@code (11 - sum) % 11} is
 * correct in languages where {@code %} returns non-negative values, but in
 * Java it silently produces wrong digits for some inputs because {@code %}
 * can return negative numbers. {@link #mod11(int)} uses the safe equivalent
 * {@code (11 - sum % 11) % 11 % 10}, keeping every term in {@code [0, 10]}.</p>
 */
public final class Weighted {

    private Weighted() {
    }

    /**
     * Weights {@code [start, start-1, ..., start-length+1]}.
     * {@code descending(10, 9)} is {@code [10..2]}.
     */
    public static int[] descending(int start, int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("length must be positive: " + length);
        }
        if (start < length) {
            throw new IllegalArgumentException(
                    "start must be >= length so weights stay positive: start="
                            + start + ", length=" + length);
        }
        int[] weights = new int[length];
        for (int i = 0; i < length; i++) {
            weights[i] = start - i;
        }
        return weights;
    }

    /**
     * Weights built by repeating {@code cycle} from the <em>rightmost</em>
     * position leftwards. {@code cyclic(12, 2,3,4,5,6,7,8,9)} is
     * {@code [5,4,3,2,9,8,7,6,5,4,3,2]}.
     */
    public static int[] cyclic(int length, int... cycle) {
        if (length <= 0) {
            throw new IllegalArgumentException("length must be positive: " + length);
        }
        if (cycle == null || cycle.length == 0) {
            throw new IllegalArgumentException("cycle must not be empty");
        }
        int[] weights = new int[length];
        for (int i = 0; i < length; i++) {
            weights[length - 1 - i] = cycle[i % cycle.length];
        }
        return weights;
    }

    /**
     * The weighted sum of the number: {@code sum((char - 48) * weight)}.
     * The number must contain only {@code '0'-'9'} and {@code 'A'-'Z'} and
     * have exactly {@code weights.length} characters.
     *
     * @throws InvalidLengthException if the lengths do not match
     * @throws InvalidFormatException on {@code null} or characters outside the alphabet
     */
    public static int weightedSum(String number, int[] weights) {
        if (number == null) {
            throw new InvalidFormatException("The number is null.");
        }
        if (number.length() != weights.length) {
            throw new InvalidLengthException();
        }
        int sum = 0;
        for (int i = 0; i < weights.length; i++) {
            char c = number.charAt(i);
            boolean valid = (c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z');
            if (!valid) {
                throw new InvalidFormatException();
            }
            sum += (c - 48) * weights[i];
        }
        return sum;
    }

    /**
     * The standard national check digit rule: {@code 11 - (sum mod 11)},
     * with results 10 and 11 mapped to 0. Implemented as
     * {@code (11 - sum % 11) % 11 % 10} so it is safe for any non-negative sum.
     */
    public static int mod11(int weightedSum) {
        return (11 - weightedSum % 11) % 11 % 10;
    }

    /** Convenience: {@link #mod11(int)} of {@link #weightedSum(String, int[])}. */
    public static int mod11CheckDigit(String number, int[] weights) {
        return mod11(weightedSum(number, weights));
    }
}
