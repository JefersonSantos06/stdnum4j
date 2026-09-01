package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;

/**
 * CUI / CIF (Codul Unic de Înregistrare / Codul de identificare fiscală),
 * the Romanian company identifier and VAT number: two to ten digits with a
 * weighted check digit, optionally prefixed {@code RO} to signal VAT
 * registration.
 */
public final class RoCui implements StdNum {

    public static final RoCui INSTANCE = new RoCui();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("ro.cui", "CUI")
                    .country("RO")
                    .title("Codul Unic de Înregistrare")
                    .description("Romanian company identifier and VAT number: 2 to 10 digits"
                            + " with a weighted mod 11 check digit.")
                    .tags(Tag.VAT, Tag.COMPANY)
                    .build();

    private static final int[] WEIGHTS = {7, 5, 3, 2, 1, 7, 5, 3, 2};

    private RoCui() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        String n = Strings.compact(number, " -").toUpperCase(Locale.ROOT);
        return n.startsWith("RO") ? n.substring(2) : n;
    }

    /** The check digit for a base of up to nine digits (zero-padded to nine). */
    public static char calcCheckDigit(String base) {
        String b = base.length() < 9 ? "0".repeat(9 - base.length()) + base : base;
        int sum = 0;
        for (int i = 0; i < WEIGHTS.length; i++) {
            sum += WEIGHTS[i] * (b.charAt(i) - '0');
        }
        return (char) ('0' + 10 * sum % 11 % 10);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n) || n.charAt(0) == '0') {
            throw new InvalidFormatException();
        }
        if (n.length() < 2 || n.length() > 10) {
            throw new InvalidLengthException();
        }
        if (n.charAt(n.length() - 1) != calcCheckDigit(n.substring(0, n.length() - 1))) {
            throw new InvalidChecksumException();
        }
        return n;
    }
}
