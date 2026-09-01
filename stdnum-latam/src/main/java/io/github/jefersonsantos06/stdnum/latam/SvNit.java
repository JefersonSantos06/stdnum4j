package io.github.jefersonsantos06.stdnum.latam;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;

/**
 * NIT (Número de Identificación Tributaria), the Salvadoran tax number:
 * fourteen digits — a four-digit municipality (starting 0 or 1 for
 * nationals, 9 for foreigners), a six-digit date in DDMMYY, a three-digit
 * sequence and a check digit.
 *
 * <p>The check digit uses one of two weight sets, chosen by whether the
 * sequence number is at most 100 — the boundary between the old and the
 * current numbering.</p>
 */
public final class SvNit implements StdNum {

    public static final SvNit INSTANCE = new SvNit();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("sv.nit", "NIT")
                    .country("SV")
                    .title("Número de Identificación Tributaria")
                    .description("Salvadoran tax number: 14 digits with a weighted mod 11"
                            + " check digit in one of two schemes.")
                    .tags(Tag.TAX, Tag.VAT)
                    .build();

    private static final int[] OLD_WEIGHTS = {14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] NEW_WEIGHTS = {2, 7, 6, 5, 4, 3, 2, 7, 6, 5, 4, 3, 2};

    private SvNit() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        String n = Strings.compact(number, " -").toUpperCase(Locale.ROOT);
        return n.startsWith("SV") ? n.substring(2) : n;
    }

    /** The check digit for a number whose first thirteen digits are known. */
    public static char calcCheckDigit(String number) {
        String n = INSTANCE.compact(number);
        boolean old = n.substring(10, 13).compareTo("100") <= 0;
        int[] weights = old ? OLD_WEIGHTS : NEW_WEIGHTS;
        int total = 0;
        for (int i = 0; i < weights.length && i < n.length(); i++) {
            total += weights[i] * (n.charAt(i) - '0');
        }
        return (char) ('0' + (old ? total % 11 % 10 : Math.floorMod(-total, 11) % 10));
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 14) {
            throw new InvalidLengthException();
        }
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if ("019".indexOf(n.charAt(0)) < 0) {
            throw new InvalidComponentException("A NIT starts with 0, 1 or 9.");
        }
        if (n.charAt(13) != calcCheckDigit(n)) {
            throw new InvalidChecksumException();
        }
        return n;
    }

    @Override
    public String format(String number) {
        String n = validate(number);
        return n.substring(0, 4) + "-" + n.substring(4, 10) + "-"
                + n.substring(10, 13) + "-" + n.substring(13);
    }
}
