package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

/**
 * ESR, the reference number on a Swiss payment slip: up to 27 digits closed
 * by a recursive mod 10 check digit.
 *
 * <p>The number is printed zero-padded to its full width on the slip, but the
 * leading zeros carry nothing and are dropped when it is compacted.</p>
 */
public final class ChEsr implements StdNum {

    public static final ChEsr INSTANCE = new ChEsr();

    /** The carry table of the recursive mod 10 algorithm. */
    private static final int[] CARRY = {0, 9, 4, 6, 8, 2, 7, 1, 3, 5};

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("ch.esr", "ESR")
                    .country("CH")
                    .title("Einzahlungsschein mit Referenznummer")
                    .description("Reference number of a Swiss payment slip: up to 27 digits"
                            + " with a recursive mod 10 check digit.")
                    .tags(Tag.PAYMENT, Tag.BANK)
                    .references("https://www.paymentstandards.ch/")
                    .build();

    private ChEsr() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        String n = Strings.clean(number, " ");
        int start = 0;
        while (start < n.length() && n.charAt(start) == '0') {
            start++;
        }
        return n.substring(start);
    }

    /** The check digit of a reference, from the digits before it. */
    public static char calcCheckDigit(String base) {
        String n = INSTANCE.compact(base);
        int carry = 0;
        for (int i = 0; i < n.length(); i++) {
            int digit = n.charAt(i) - '0';
            if (digit < 0 || digit > 9) {
                throw new InvalidFormatException();
            }
            carry = CARRY[(digit + carry) % 10];
        }
        return (char) ('0' + (10 - carry) % 10);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() > 27) {
            throw new InvalidLengthException();
        }
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.charAt(n.length() - 1) != calcCheckDigit(n.substring(0, n.length() - 1))) {
            throw new InvalidChecksumException();
        }
        return n;
    }

    @Override
    public String format(String number) {
        String n = validate(number);
        String padded = "0".repeat(27 - n.length()) + n;
        StringBuilder sb = new StringBuilder(padded.substring(0, 2));
        for (int i = 2; i < 27; i += 5) {
            sb.append(' ').append(padded, i, i + 5);
        }
        return sb.toString();
    }
}
