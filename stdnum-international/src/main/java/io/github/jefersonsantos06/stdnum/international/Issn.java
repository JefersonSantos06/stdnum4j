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
 * ISSN (International Standard Serial Number), identifying periodical
 * publications: eight characters, the last a mod 11 check character that
 * may be {@code X}, conventionally printed as two groups of four.
 */
public final class Issn implements StdNum {

    public static final Issn INSTANCE = new Issn();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("issn", "ISSN")
                    .title("International Standard Serial Number")
                    .description("Periodical publication identifier: 8 characters with a"
                            + " mod 11 check character that may be X.")
                    .tags(Tag.MEDIA)
                    .references("https://en.wikipedia.org/wiki/International_Standard_Serial_Number")
                    .build();

    private Issn() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -").toUpperCase(Locale.ROOT);
    }

    /** The check character for the seven-digit base. */
    public static char calcCheckDigit(String base) {
        int sum = 0;
        for (int i = 0; i < base.length(); i++) {
            sum += (8 - i) * (base.charAt(i) - '0');
        }
        int check = Math.floorMod(11 - sum, 11);
        return check == 10 ? 'X' : (char) ('0' + check);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.isEmpty() || !Strings.isDigits(n.substring(0, n.length() - 1))) {
            throw new InvalidFormatException();
        }
        if (n.length() != 8) {
            throw new InvalidLengthException();
        }
        if (n.charAt(7) != calcCheckDigit(n.substring(0, 7))) {
            throw new InvalidChecksumException();
        }
        return n;
    }

    @Override
    public String format(String number) {
        String n = validate(number);
        return n.substring(0, 4) + "-" + n.substring(4);
    }

    /** The 13-digit EAN of this ISSN, in the 977 bookland prefix. */
    public static String toEan(String number) {
        String base = "977" + INSTANCE.validate(number).substring(0, 7) + "00";
        return base + Ean.calcCheckDigit(base);
    }
}
