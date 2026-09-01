package io.github.jefersonsantos06.stdnum.africa;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

/**
 * NUIT (Número Único de Identificação Tributária), the Mozambican tax
 * number: nine digits where the first gives the type of entity and the
 * last is a weighted mod 11 check digit.
 */
public final class MzNuit implements StdNum {

    public static final MzNuit INSTANCE = new MzNuit();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("mz.nuit", "NUIT")
                    .country("MZ")
                    .title("Número Único de Identificação Tributária")
                    .description("Mozambican tax number: 9 digits with a weighted mod 11"
                            + " check digit.")
                    .tags(Tag.TAX, Tag.VAT)
                    .build();

    private static final int[] WEIGHTS = {8, 9, 4, 5, 6, 7, 8, 9};

    private MzNuit() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -.");
    }

    /** The check digit for the eight-digit base. */
    public static char calcCheckDigit(String base) {
        int sum = 0;
        for (int i = 0; i < WEIGHTS.length && i < base.length(); i++) {
            sum += WEIGHTS[i] * (base.charAt(i) - '0');
        }
        return "01234567891".charAt(sum % 11);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 9) {
            throw new InvalidLengthException();
        }
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.charAt(8) != calcCheckDigit(n.substring(0, 8))) {
            throw new InvalidChecksumException();
        }
        return n;
    }

    @Override
    public String format(String number) {
        String n = validate(number);
        return n.substring(0, 3) + " " + n.substring(3, 6) + " " + n.substring(6);
    }
}
