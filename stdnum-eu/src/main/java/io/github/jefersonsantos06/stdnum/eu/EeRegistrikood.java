package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

/**
 * Registrikood, the Estonian organisation registry code: eight digits opening
 * with 1, 7, 8 or 9 — the digit tells apart companies, non-profits, state
 * agencies and the like — and closing with the check digit Estonia also uses
 * on the personal {@link EeIk}.
 */
public final class EeRegistrikood implements StdNum {

    public static final EeRegistrikood INSTANCE = new EeRegistrikood();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("ee.registrikood", "Registrikood")
                    .country("EE")
                    .title("Eesti registrikood")
                    .description("Estonian organisation registry code: 8 digits starting with"
                            + " 1, 7, 8 or 9, with a weighted mod 11 check digit.")
                    .tags(Tag.COMPANY)
                    .references("https://ariregister.rik.ee/")
                    .build();

    private EeRegistrikood() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " ");
    }

    /** The check digit of a number, from every digit but its last. */
    public static char calcCheckDigit(String number) {
        return EeIk.calcCheckDigit(number);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.length() != 8) {
            throw new InvalidLengthException();
        }
        if ("1789".indexOf(n.charAt(0)) < 0) {
            throw new InvalidComponentException(
                    "Estonian registry codes start with 1, 7, 8 or 9.");
        }
        if (n.charAt(7) != calcCheckDigit(n)) {
            throw new InvalidChecksumException();
        }
        return n;
    }
}
