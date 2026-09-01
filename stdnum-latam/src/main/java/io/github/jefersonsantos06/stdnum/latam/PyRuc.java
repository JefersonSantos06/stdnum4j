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
 * RUC (Registro Único de Contribuyentes), the Paraguayan taxpayer number:
 * up to nine digits — eight for legal entities, fewer for residents and
 * foreigners — closed by a check digit weighted from the right.
 */
public final class PyRuc implements StdNum {

    public static final PyRuc INSTANCE = new PyRuc();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("py.ruc", "RUC")
                    .country("PY")
                    .title("Registro Único de Contribuyentes")
                    .description("Paraguayan taxpayer number: up to 9 digits with a weighted"
                            + " mod 11 check digit.")
                    .tags(Tag.TAX, Tag.VAT)
                    .build();

    private PyRuc() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -").toUpperCase(Locale.ROOT);
    }

    /** The check digit for the base without its final digit. */
    public static char calcCheckDigit(String base) {
        int sum = 0;
        for (int i = 0; i < base.length(); i++) {
            sum += (i + 2) * (base.charAt(base.length() - 1 - i) - '0');
        }
        return (char) ('0' + Math.floorMod(-sum, 11) % 10);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.isEmpty() || n.length() > 9) {
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
        return n.substring(0, n.length() - 1) + "-" + n.charAt(n.length() - 1);
    }
}
