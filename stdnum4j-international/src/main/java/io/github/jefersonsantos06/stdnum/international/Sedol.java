package io.github.jefersonsantos06.stdnum.international;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;

/**
 * SEDOL, the security identifier assigned by the London Stock Exchange:
 * six alphanumeric characters and a check digit. Vowels are never used, so
 * a SEDOL can never be confused with a word. New-style numbers start with
 * a letter; old-style ones are entirely numeric.
 */
public final class Sedol implements StdNum {

    public static final Sedol INSTANCE = new Sedol();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("sedol", "SEDOL")
                    .title("Stock Exchange Daily Official List number")
                    .description("UK and Irish security identifier: 7 characters without"
                            + " vowels, closed by a weighted mod 10 check digit.")
                    .tags(Tag.FINANCIAL)
                    .references("https://en.wikipedia.org/wiki/SEDOL")
                    .build();

    /** Digits and consonants; the gaps are the vowels, which are never used. */
    private static final String ALPHABET = "0123456789 BCD FGH JKLMN PQRST VWXYZ";
    private static final int[] WEIGHTS = {1, 3, 1, 7, 3, 9};

    private Sedol() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " ").toUpperCase(Locale.ROOT);
    }

    /** The check digit for the six-character base. */
    public static char calcCheckDigit(String base) {
        String b = INSTANCE.compact(base);
        int sum = 0;
        for (int i = 0; i < WEIGHTS.length && i < b.length(); i++) {
            int value = ALPHABET.indexOf(b.charAt(i));
            if (value < 0) {
                throw new InvalidFormatException();
            }
            sum += WEIGHTS[i] * value;
        }
        return (char) ('0' + (10 - sum % 10) % 10);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        for (int i = 0; i < n.length(); i++) {
            if (n.charAt(i) == ' ' || ALPHABET.indexOf(n.charAt(i)) < 0) {
                throw new InvalidFormatException();
            }
        }
        if (n.length() != 7) {
            throw new InvalidLengthException();
        }
        if (Strings.isDigits(n.substring(0, 1)) && !Strings.isDigits(n)) {
            throw new InvalidFormatException(Message.of(Sedol.class, "sedol.numeric",
                    "A SEDOL starting with a digit must be entirely numeric."));
        }
        if (n.charAt(6) != calcCheckDigit(n.substring(0, 6))) {
            throw new InvalidChecksumException();
        }
        return n;
    }

    /** The ISIN of this security in the given country (usually {@code "GB"}). */
    public static String toIsin(String number, String countryCode) {
        String base = countryCode.toUpperCase(Locale.ROOT) + "00" + INSTANCE.validate(number);
        return base + Isin.calcCheckDigit(base);
    }
}
