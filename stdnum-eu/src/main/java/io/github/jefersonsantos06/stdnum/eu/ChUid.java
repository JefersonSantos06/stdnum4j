package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.Reasons;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;

/**
 * UID (Unternehmens-Identifikationsnummer), the Swiss business identifier:
 * the fixed prefix {@code CHE} plus nine digits with a weighted check
 * digit. Only the format introduced in 2011 is supported, which fully
 * replaced the old six-digit one in 2014. Numbers given without the prefix
 * are accepted and returned with it.
 */
public final class ChUid implements StdNum {

    public static final ChUid INSTANCE = new ChUid();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("ch.uid", "UID")
                    .country("CH")
                    .title("Unternehmens-Identifikationsnummer")
                    .description("Swiss business identifier: CHE plus 9 digits with a"
                            + " weighted mod 11 check digit.")
                    .tags(Tag.COMPANY)
                    .build();

    private static final int[] WEIGHTS = {5, 4, 3, 2, 7, 6, 5, 4};

    private ChUid() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        String n = Strings.compact(number, " -.").toUpperCase(Locale.ROOT);
        return n.length() == 9 && Strings.isDigits(n) ? "CHE" + n : n;
    }

    /** The check digit for the eight digits following the CHE prefix. */
    public static char calcCheckDigit(String base) {
        int sum = 0;
        for (int i = 0; i < WEIGHTS.length && i < base.length(); i++) {
            sum += WEIGHTS[i] * (base.charAt(i) - '0');
        }
        int check = Math.floorMod(11 - sum, 11);
        if (check > 9) {
            throw new InvalidChecksumException(Reasons.noCheckDigit());
        }
        return (char) ('0' + check);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 12) {
            throw new InvalidLengthException();
        }
        if (!n.startsWith("CHE")) {
            throw new InvalidComponentException(Message.of(ChUid.class, "uid.prefix",
                    "A Swiss UID starts with CHE."));
        }
        if (!Strings.isDigits(n.substring(3))) {
            throw new InvalidFormatException();
        }
        if (n.charAt(11) != calcCheckDigit(n.substring(3, 11))) {
            throw new InvalidChecksumException();
        }
        return n;
    }

    @Override
    public String format(String number) {
        String n = validate(number);
        return "CHE-" + n.substring(3, 6) + "." + n.substring(6, 9) + "." + n.substring(9);
    }
}
