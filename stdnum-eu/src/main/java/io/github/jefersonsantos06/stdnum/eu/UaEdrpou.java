package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

/**
 * ЄДРПОУ, the Ukrainian company registry number: eight digits whose check
 * digit uses weights 1..7 — rotated for numbers starting 3, 4 or 5 — and
 * falls back to a second weight set when the first yields 10.
 */
public final class UaEdrpou implements StdNum {

    public static final UaEdrpou INSTANCE = new UaEdrpou();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("ua.edrpou", "ЄДРПОУ")
                    .country("UA")
                    .title("Єдиний державний реєстр підприємств та організацій України")
                    .description("Ukrainian company registry number: 8 digits with a weighted"
                            + " mod 11 check digit and a fallback weight set.")
                    .tags(Tag.COMPANY, Tag.TAX)
                    .build();

    private UaEdrpou() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " ");
    }

    /** The check digit for a number whose first seven digits are known. */
    public static char calcCheckDigit(String number) {
        String n = INSTANCE.compact(number);
        int[] weights = "345".indexOf(n.charAt(0)) >= 0
                ? new int[] {7, 1, 2, 3, 4, 5, 6}
                : new int[] {1, 2, 3, 4, 5, 6, 7};
        int total = weightedSum(n, weights);
        if (total % 11 < 10) {
            return (char) ('0' + total % 11);
        }
        for (int i = 0; i < weights.length; i++) {
            weights[i] += 2;
        }
        return (char) ('0' + weightedSum(n, weights) % 11 % 10);
    }

    private static int weightedSum(String n, int[] weights) {
        int sum = 0;
        for (int i = 0; i < weights.length && i < n.length(); i++) {
            sum += weights[i] * (n.charAt(i) - '0');
        }
        return sum;
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 8) {
            throw new InvalidLengthException();
        }
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.charAt(7) != calcCheckDigit(n)) {
            throw new InvalidChecksumException();
        }
        return n;
    }
}
