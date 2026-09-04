package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

/**
 * PIB (Poreski identifikacioni broj), the Montenegrin tax number: eight
 * digits weighted 8..2 modulo 11.
 */
public final class MePib implements StdNum {

    public static final MePib INSTANCE = new MePib();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("me.pib", "PIB")
                    .country("ME")
                    .title("Poreski identifikacioni broj")
                    .description("Montenegrin tax number: 8 digits with a weighted mod 11"
                            + " check digit.")
                    .tags(Tag.TAX, Tag.VAT)
                    .build();

    private static final int[] WEIGHTS = {8, 7, 6, 5, 4, 3, 2};

    private MePib() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " ");
    }

    /** The check digit for the seven-digit base. */
    public static char calcCheckDigit(String base) {
        int sum = 0;
        for (int i = 0; i < WEIGHTS.length && i < base.length(); i++) {
            sum += WEIGHTS[i] * (base.charAt(i) - '0');
        }
        return (char) ('0' + Math.floorMod(-sum, 11) % 10);
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
