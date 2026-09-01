package io.github.jefersonsantos06.stdnum.latam;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;

/**
 * NIT (Número De Identificación Tributaria), also called RUT, the Colombian
 * business tax number: 8 to 16 digits closed by a check digit weighted with
 * ascending primes from the right.
 */
public final class CoNit implements StdNum {

    public static final CoNit INSTANCE = new CoNit();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("co.nit", "NIT")
                    .country("CO")
                    .title("Número De Identificación Tributaria")
                    .description("Colombian business tax number: 8 to 16 digits with a"
                            + " prime-weighted mod 11 check digit.")
                    .tags(Tag.TAX, Tag.VAT, Tag.COMPANY)
                    .build();

    private static final int[] WEIGHTS =
            {3, 7, 13, 17, 19, 23, 29, 37, 41, 43, 47, 53, 59, 67, 71};

    private CoNit() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, ".,- ").toUpperCase(Locale.ROOT);
    }

    /** The check digit for the base without its final digit. */
    public static char calcCheckDigit(String base) {
        int sum = 0;
        for (int i = 0; i < WEIGHTS.length && i < base.length(); i++) {
            sum += WEIGHTS[i] * (base.charAt(base.length() - 1 - i) - '0');
        }
        return "01987654321".charAt(sum % 11);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() < 8 || n.length() > 16) {
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
        String base = n.substring(0, n.length() - 1);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < base.length(); i++) {
            if (i > 0 && (base.length() - i) % 3 == 0) {
                sb.append('.');
            }
            sb.append(base.charAt(i));
        }
        return sb.append('-').append(n.charAt(n.length() - 1)).toString();
    }
}
