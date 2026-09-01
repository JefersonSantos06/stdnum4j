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
 * NIT (Número de Identificación Tributaria), the Guatemalan tax number: 2
 * to 12 characters, all digits except the last, which is a digit or the
 * letter {@code K}. Leading zeros are conventionally omitted and stripped
 * here.
 */
public final class GtNit implements StdNum {

    public static final GtNit INSTANCE = new GtNit();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("gt.nit", "NIT")
                    .country("GT")
                    .title("Número de Identificación Tributaria")
                    .description("Guatemalan tax number: up to 12 characters with a mod 11"
                            + " check digit that may be K.")
                    .tags(Tag.TAX, Tag.VAT)
                    .build();

    private GtNit() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        String n = Strings.compact(number, " -").toUpperCase(Locale.ROOT);
        int i = 0;
        while (i < n.length() - 1 && n.charAt(i) == '0') {
            i++;
        }
        return n.substring(i);
    }

    /** The check character for the base without its final character. */
    public static char calcCheckDigit(String base) {
        int sum = 0;
        for (int i = 0; i < base.length(); i++) {
            sum += (i + 2) * (base.charAt(base.length() - 1 - i) - '0');
        }
        int check = Math.floorMod(-sum, 11);
        return check == 10 ? 'K' : (char) ('0' + check);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() < 2 || n.length() > 12) {
            throw new InvalidLengthException();
        }
        if (!Strings.isDigits(n.substring(0, n.length() - 1))) {
            throw new InvalidFormatException();
        }
        char last = n.charAt(n.length() - 1);
        if (last != 'K' && (last < '0' || last > '9')) {
            throw new InvalidFormatException();
        }
        if (last != calcCheckDigit(n.substring(0, n.length() - 1))) {
            throw new InvalidChecksumException();
        }
        return n;
    }

    @Override
    public String format(String number) {
        String n = validate(number);
        return n.substring(0, n.length() - 1) + "-" + n.charAt(n.length() - 1);
    }
}
