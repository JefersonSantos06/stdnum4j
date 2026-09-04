package io.github.jefersonsantos06.stdnum.latam;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Resources;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Set;

/**
 * RNC (Registro Nacional del Contribuyente), the Dominican tax number:
 * nine digits closed by a weighted mod 11 check digit.
 *
 * <p>A companion file lists the RNCs the DGII issued that do not satisfy
 * the check digit; they are accepted before the checksum runs.</p>
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

    /** RNCs the DGII issued that do not satisfy the check digit. */
    private static Set<String> whitelist() {
        return Resources.lines(DoRnc.class, "do-rnc-whitelist.txt");
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        // consulted before length and checksum: some entries are shorter
        if (whitelist().contains(n)) {
            return n;
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
        // a few whitelisted RNCs are shorter than the canonical nine digits
        return n.length() == 9
                ? n.charAt(0) + "-" + n.substring(1, 3) + "-" + n.substring(3, 8)
                        + "-" + n.charAt(8)
                : n;
    }
}
