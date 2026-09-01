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
 * ЕДБ (Единствен даночен број), the North Macedonian tax number: thirteen
 * digits weighted 7..2 twice, modulo 11. Both the Latin {@code MK} and the
 * Cyrillic {@code МК} prefixes are accepted.
 */
public final class MkEdb implements StdNum {

    public static final MkEdb INSTANCE = new MkEdb();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("mk.edb", "ЕДБ")
                    .country("MK")
                    .title("Единствен даночен број")
                    .description("North Macedonian tax number: 13 digits with a weighted"
                            + " mod 11 check digit.")
                    .tags(Tag.TAX, Tag.VAT)
                    .build();

    private static final int[] WEIGHTS = {7, 6, 5, 4, 3, 2, 7, 6, 5, 4, 3, 2};

    private MkEdb() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        String n = Strings.compact(number, " -").toUpperCase(Locale.ROOT);
        // the Cyrillic МК looks identical to the Latin MK but is a different prefix
        if (n.startsWith("MK") || n.startsWith("МК")) {
            return n.substring(2);
        }
        return n;
    }

    /** The check digit for the twelve-digit base. */
    public static char calcCheckDigit(String base) {
        int sum = 0;
        for (int i = 0; i < WEIGHTS.length && i < base.length(); i++) {
            sum += WEIGHTS[i] * (base.charAt(i) - '0');
        }
        return (char) ('0' + Math.floorMod(-sum, 11) % 10);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 13) {
            throw new InvalidLengthException();
        }
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.charAt(12) != calcCheckDigit(n)) {
            throw new InvalidChecksumException();
        }
        return n;
    }
}
