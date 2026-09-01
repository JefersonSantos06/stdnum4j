package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

/**
 * Registrikood, the Estonian organisation registry code: eight digits opening
 * with 1, 7, 8 or 9 — the digit tells apart companies, non-profits, state
 * agencies and the like — and closing with the same check digit Estonia uses
 * on personal identity codes.
 */
public final class EeRegistrikood implements StdNum {

    public static final EeRegistrikood INSTANCE = new EeRegistrikood();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("ee.registrikood", "Registrikood")
                    .country("EE")
                    .title("Eesti registrikood")
                    .description("Estonian organisation registry code: 8 digits starting with"
                            + " 1, 7, 8 or 9, with a weighted mod 11 check digit.")
                    .tags(Tag.COMPANY)
                    .references("https://ariregister.rik.ee/")
                    .build();

    private EeRegistrikood() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " ");
    }

    /**
     * The check digit of a number, from every digit but its last. The weights
     * run 1..9 cyclically; when that yields 10 the sum is taken again with
     * the weights shifted by two.
     */
    public static char calcCheckDigit(String number) {
        String n = INSTANCE.compact(number);
        int check = weightedSum(n, 1) % 11;
        if (check == 10) {
            check = weightedSum(n, 3) % 11;
        }
        return (char) ('0' + check % 10);
    }

    /** The digits of {@code n} bar the last, weighted from {@code first} on, cycling 1..9. */
    private static int weightedSum(String n, int first) {
        int sum = 0;
        for (int i = 0; i < n.length() - 1; i++) {
            sum += ((i + first - 1) % 9 + 1) * (n.charAt(i) - '0');
        }
        return sum;
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.length() != 8) {
            throw new InvalidLengthException();
        }
        if ("1789".indexOf(n.charAt(0)) < 0) {
            throw new InvalidComponentException(
                    "Estonian registry codes start with 1, 7, 8 or 9.");
        }
        if (n.charAt(7) != calcCheckDigit(n)) {
            throw new InvalidChecksumException();
        }
        return n;
    }
}
