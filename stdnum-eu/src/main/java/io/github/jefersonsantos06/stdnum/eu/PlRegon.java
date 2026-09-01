package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

/**
 * REGON (Rejestr Gospodarki Narodowej), the Polish statistical number for
 * businesses: nine digits for national entities, or fourteen when a local
 * unit appends five more. Both the 9th and the 14th digit are check digits.
 */
public final class PlRegon implements StdNum {

    public static final PlRegon INSTANCE = new PlRegon();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("pl.regon", "REGON")
                    .country("PL")
                    .title("Rejestr Gospodarki Narodowej")
                    .description("Polish business statistical number: 9 or 14 digits, each"
                            + " block closed by a weighted mod 11 check digit.")
                    .tags(Tag.COMPANY)
                    .build();

    private static final int[] WEIGHTS_8 = {8, 9, 2, 3, 4, 5, 6, 7};
    private static final int[] WEIGHTS_13 = {2, 4, 8, 5, 0, 9, 7, 3, 6, 1, 2, 4, 8};

    private PlRegon() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -");
    }

    /** The check digit for an 8- or 13-digit base. */
    public static char calcCheckDigit(String base) {
        int[] weights = base.length() == 8 ? WEIGHTS_8 : WEIGHTS_13;
        int sum = 0;
        for (int i = 0; i < weights.length && i < base.length(); i++) {
            sum += weights[i] * (base.charAt(i) - '0');
        }
        return (char) ('0' + sum % 11 % 10);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.length() != 9 && n.length() != 14) {
            throw new InvalidLengthException();
        }
        if (n.charAt(n.length() - 1) != calcCheckDigit(n.substring(0, n.length() - 1))) {
            throw new InvalidChecksumException();
        }
        if (n.length() == 14 && n.charAt(8) != calcCheckDigit(n.substring(0, 8))) {
            throw new InvalidChecksumException();
        }
        return n;
    }
}
