package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

/**
 * IDNO (Numărul de identificare de stat), the Moldovan state identity
 * number for legal entities: thirteen digits weighted 7,3,1 repeating,
 * modulo 10.
 */
public final class MdIdno implements StdNum {

    public static final MdIdno INSTANCE = new MdIdno();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("md.idno", "IDNO")
                    .country("MD")
                    .title("Numărul de identificare de stat")
                    .description("Moldovan company identity number: 13 digits with a weighted"
                            + " mod 10 check digit.")
                    .tags(Tag.COMPANY, Tag.TAX, Tag.VAT)
                    .build();

    private static final int[] WEIGHTS = {7, 3, 1, 7, 3, 1, 7, 3, 1, 7, 3, 1};

    private MdIdno() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " ");
    }

    /** The check digit for the twelve-digit base. */
    public static char calcCheckDigit(String base) {
        int sum = 0;
        for (int i = 0; i < WEIGHTS.length && i < base.length(); i++) {
            sum += WEIGHTS[i] * (base.charAt(i) - '0');
        }
        return (char) ('0' + sum % 10);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.length() != 13) {
            throw new InvalidLengthException();
        }
        if (n.charAt(12) != calcCheckDigit(n)) {
            throw new InvalidChecksumException();
        }
        return n;
    }
}
