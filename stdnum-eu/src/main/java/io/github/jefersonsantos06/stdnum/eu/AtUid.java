package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.algo.Luhn;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;

/**
 * UID (Umsatzsteuer-Identifikationsnummer), the Austrian VAT number: the
 * letter {@code U} followed by eight digits, the last one a check digit
 * derived from the Luhn checksum of the preceding seven.
 */
public final class AtUid implements StdNum {

    public static final AtUid INSTANCE = new AtUid();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("at.uid", "UID")
                    .country("AT")
                    .title("Umsatzsteuer-Identifikationsnummer")
                    .description("Austrian VAT number: U followed by 8 digits with a"
                            + " Luhn-derived check digit.")
                    .tags(Tag.VAT)
                    .build();

    private AtUid() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        String n = Strings.compact(number, " -./").toUpperCase(Locale.ROOT);
        return n.startsWith("AT") ? n.substring(2) : n;
    }

    /** The check digit for a base of {@code U} plus seven digits. */
    public static char calcCheckDigit(String base) {
        String b = INSTANCE.compact(base);
        if (b.length() != 8 || b.charAt(0) != 'U' || !Strings.isDigits(b.substring(1))) {
            throw new InvalidFormatException();
        }
        return (char) ('0' + Math.floorMod(6 - Luhn.checksum(b.substring(1)), 10));
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.isEmpty() || n.charAt(0) != 'U' || !Strings.isDigits(n.substring(1))) {
            throw new InvalidFormatException();
        }
        if (n.length() != 9) {
            throw new InvalidLengthException();
        }
        if (n.charAt(8) != calcCheckDigit(n.substring(0, 8))) {
            throw new InvalidChecksumException();
        }
        return n;
    }
}
