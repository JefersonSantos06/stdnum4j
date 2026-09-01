package io.github.jefersonsantos06.stdnum.latam;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

/**
 * RNC (Registro Nacional del Contribuyente), the Dominican tax number:
 * nine digits closed by a weighted mod 11 check digit.
 */
public final class DoRnc implements StdNum {

    public static final DoRnc INSTANCE = new DoRnc();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("do.rnc", "RNC")
                    .country("DO")
                    .title("Registro Nacional del Contribuyente")
                    .description("Dominican tax number: 9 digits with a weighted mod 11"
                            + " check digit.")
                    .tags(Tag.TAX, Tag.VAT, Tag.COMPANY)
                    .build();

    private static final int[] WEIGHTS = {7, 9, 8, 6, 5, 4, 3, 2};

    private DoRnc() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -");
    }

    /** The check digit for the eight-digit base. */
    public static char calcCheckDigit(String base) {
        int sum = 0;
        for (int i = 0; i < WEIGHTS.length && i < base.length(); i++) {
            sum += WEIGHTS[i] * (base.charAt(i) - '0');
        }
        return (char) ('0' + (10 - sum % 11) % 9 + 1);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.length() != 9) {
            throw new InvalidLengthException();
        }
        if (n.charAt(8) != calcCheckDigit(n)) {
            throw new InvalidChecksumException();
        }
        return n;
    }

    @Override
    public String format(String number) {
        String n = validate(number);
        return n.charAt(0) + "-" + n.substring(1, 3) + "-" + n.substring(3, 8)
                + "-" + n.charAt(8);
    }
}
