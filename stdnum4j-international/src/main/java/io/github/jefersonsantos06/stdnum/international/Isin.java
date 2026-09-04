package io.github.jefersonsantos06.stdnum.international;

import io.github.jefersonsantos06.stdnum.algo.Luhn;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;

/**
 * ISIN (International Securities Identification Number, ISO 6166).
 *
 * <p>A two-letter country (or XS-style) prefix, a nine-character national
 * security identifier and one check digit: every character is expanded to
 * its base-36 value and the resulting digit string must pass the Luhn
 * check.</p>
 */
public final class Isin implements StdNum {

    public static final Isin INSTANCE = new Isin();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("isin", "ISIN")
                    .title("International Securities Identification Number")
                    .description("ISO 6166 securities identifier: 12 characters closed by a"
                            + " Luhn check digit over the base-36 expansion.")
                    .tags(Tag.FINANCIAL)
                    .references("https://en.wikipedia.org/wiki/International_Securities_Identification_Number")
                    .build();

    private Isin() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -").toUpperCase(Locale.ROOT);
    }

    /** The check digit for the 11-character base. */
    public static char calcCheckDigit(String base) {
        String b = INSTANCE.compact(base);
        if (b.length() != 11) {
            throw new InvalidLengthException();
        }
        return Luhn.calcCheckDigit(expand(b));
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 12) {
            throw new InvalidLengthException();
        }
        if (!Character.isLetter(n.charAt(0)) || !Character.isLetter(n.charAt(1))
                || !Character.isDigit(n.charAt(11))) {
            throw new InvalidFormatException();
        }
        Luhn.validate(expand(n));
        return n;
    }

    /** Expands letters to their base-36 value (A=10 ... Z=35). */
    private static String expand(String n) {
        StringBuilder sb = new StringBuilder(n.length() * 2);
        for (int i = 0; i < n.length(); i++) {
            int value = Character.digit(n.charAt(i), 36);
            if (value < 0) {
                throw new InvalidFormatException();
            }
            sb.append(value);
        }
        return sb.toString();
    }

    /**
     * Builds an ISIN from a national security identifier, zero-padding the
     * identifier to the nine characters the format reserves for it.
     */
    public static String fromNationalId(String countryCode, String number) {
        String n = INSTANCE.compact(number);
        String padded = n.length() < 9 ? "0".repeat(9 - n.length()) + n : n;
        String base = countryCode.toUpperCase(Locale.ROOT) + padded;
        return base + calcCheckDigit(base);
    }

}
