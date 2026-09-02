package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Reasons;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Maticna stevilka, the Slovenian business register number: seven digits
 * closing with a check digit, optionally followed by a three-character
 * subunit code. The plain company itself carries the subunit 000, which is
 * dropped when the number is compacted.
 */
public final class SiMaticna implements StdNum {

    public static final SiMaticna INSTANCE = new SiMaticna();

    private static final int[] WEIGHTS = {7, 6, 5, 4, 3, 2};
    private static final Pattern SUBUNIT = Pattern.compile("[A-Z0-9][0-9]{2}");

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("si.maticna", "Maticna stevilka")
                    .country("SI")
                    .title("Slovenska maticna stevilka")
                    .description("Slovenian business register number: 7 digits with a weighted"
                            + " mod 11 check digit, plus an optional 3-character subunit code.")
                    .tags(Tag.COMPANY)
                    .references("https://www.uradni-list.si/glasilo-uradni-list-rs/vsebina/2002-01-5722/"
                            + "metodolosko-navodilo-za-vodenje-poslovnega-registra-slovenije")
                    .build();

    private SiMaticna() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        String n = Strings.compact(number, ". ").toUpperCase(Locale.ROOT);
        // 000 is the company itself rather than one of its establishments
        return n.length() == 10 && n.endsWith("000") ? n.substring(0, 7) : n;
    }

    /**
     * The check digit in seventh position, from the six digits before it.
     *
     * @throws InvalidChecksumException when the weighted sum is a multiple of
     *                                  11, for which no check digit exists
     */
    public static char calcCheckDigit(String number) {
        String n = INSTANCE.compact(number);
        int total = 0;
        for (int i = 0; i < WEIGHTS.length && i < n.length(); i++) {
            total += WEIGHTS[i] * (n.charAt(i) - '0');
        }
        int remainder = Math.floorMod(-total, 11);
        if (remainder == 0) {
            throw new InvalidChecksumException(Reasons.noCheckDigit());
        }
        return (char) ('0' + remainder % 10);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 7 && n.length() != 10) {
            throw new InvalidLengthException();
        }
        if (!Strings.isDigits(n.substring(0, 6))) {
            throw new InvalidFormatException();
        }
        if (n.length() == 10 && !SUBUNIT.matcher(n.substring(7)).matches()) {
            throw new InvalidFormatException();
        }
        if (n.charAt(6) != calcCheckDigit(n)) {
            throw new InvalidChecksumException();
        }
        return n;
    }
}
