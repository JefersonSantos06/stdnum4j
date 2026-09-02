package io.github.jefersonsantos06.stdnum.international;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;
import java.util.Set;

/**
 * FIGI (Financial Instrument Global Identifier): twelve characters over an
 * alphabet without vowels, where the first two are a consonant pair that
 * is not a reserved ISO country code, the third is always {@code G}, and
 * the last is a doubling mod 10 check digit.
 */
public final class Figi implements StdNum {

    public static final Figi INSTANCE = new Figi();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("figi", "FIGI")
                    .title("Financial Instrument Global Identifier")
                    .description("Financial instrument identifier: 12 characters without"
                            + " vowels, with a doubling mod 10 check digit.")
                    .tags(Tag.FINANCIAL)
                    .references("https://en.wikipedia.org/wiki/Financial_Instrument_Global_Identifier")
                    .build();

    private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String ALLOWED = "0123456789BCDFGHJKLMNPQRSTVWXYZ";

    /** Prefixes reserved because they collide with ISO country codes. */
    private static final Set<String> RESERVED_PREFIXES =
            Set.of("BS", "BM", "GG", "GB", "VG");

    private Figi() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " ").toUpperCase(Locale.ROOT);
    }

    /** The check digit for the eleven-character base. */
    public static char calcCheckDigit(String base) {
        String b = INSTANCE.compact(base);
        int sum = 0;
        for (int i = 0; i < 11 && i < b.length(); i++) {
            int value = ALPHABET.indexOf(b.charAt(i));
            if (value < 0) {
                throw new InvalidFormatException();
            }
            int product = value * (i % 2 == 0 ? 1 : 2);
            while (product > 0) {
                sum += product % 10;
                product /= 10;
            }
        }
        return (char) ('0' + (10 - sum % 10) % 10);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        for (int i = 0; i < n.length(); i++) {
            if (ALLOWED.indexOf(n.charAt(i)) < 0) {
                throw new InvalidFormatException();
            }
        }
        if (n.length() != 12) {
            throw new InvalidLengthException();
        }
        if (Character.isDigit(n.charAt(0)) || Character.isDigit(n.charAt(1))) {
            throw new InvalidFormatException(Message.of(Figi.class, "figi.letters",
                    "A FIGI starts with two letters."));
        }
        if (RESERVED_PREFIXES.contains(n.substring(0, 2))) {
            throw new InvalidComponentException(Message.of(Figi.class, "figi.reserved-prefix",
                    "This prefix is reserved."));
        }
        if (n.charAt(2) != 'G') {
            throw new InvalidComponentException(Message.of(Figi.class, "figi.g",
                    "The third character of a FIGI is G."));
        }
        if (n.charAt(11) != calcCheckDigit(n.substring(0, 11))) {
            throw new InvalidChecksumException();
        }
        return n;
    }
}
