package io.github.jefersonsantos06.stdnum.international;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;

/**
 * CUSIP number (Committee on Uniform Securities Identification
 * Procedures), identifying North American financial securities: six
 * characters for the issuer, two for the issue and a check digit computed
 * by doubling alternate positions and summing the resulting digits.
 */
public final class Cusip implements StdNum {

    public static final Cusip INSTANCE = new Cusip();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("cusip", "CUSIP")
                    .title("CUSIP number")
                    .description("North American securities identifier: 9 characters with a"
                            + " doubling mod 10 check digit.")
                    .tags(Tag.FINANCIAL)
                    .references("https://en.wikipedia.org/wiki/CUSIP")
                    .build();

    private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ*@#";

    private Cusip() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " ").toUpperCase(Locale.ROOT);
    }

    /** The check digit for the eight-character base. */
    public static char calcCheckDigit(String base) {
        String b = INSTANCE.compact(base);
        int sum = 0;
        for (int i = 0; i < b.length(); i++) {
            int value = ALPHABET.indexOf(b.charAt(i));
            if (value < 0) {
                throw new InvalidFormatException();
            }
            int doubled = (i % 2 == 0 ? 1 : 2) * value;
            // sum the decimal digits of the (possibly multi-digit) product
            while (doubled > 0) {
                sum += doubled % 10;
                doubled /= 10;
            }
        }
        return (char) ('0' + (10 - sum % 10) % 10);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        for (int i = 0; i < n.length(); i++) {
            if (ALPHABET.indexOf(n.charAt(i)) < 0) {
                throw new InvalidFormatException();
            }
        }
        if (n.length() != 9) {
            throw new InvalidLengthException();
        }
        if (n.charAt(8) != calcCheckDigit(n.substring(0, 8))) {
            throw new InvalidChecksumException();
        }
        return n;
    }

    /** The ISIN of this security in the given country (usually {@code "US"}). */
    public static String toIsin(String number, String countryCode) {
        String base = countryCode.toUpperCase(Locale.ROOT) + INSTANCE.validate(number);
        return base + Isin.calcCheckDigit(base);
    }
}
