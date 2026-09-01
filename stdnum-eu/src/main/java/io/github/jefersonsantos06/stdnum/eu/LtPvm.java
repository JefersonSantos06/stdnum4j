package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;

/**
 * PVM (Pridėtinės vertės mokestis mokėtojo kodas), the Lithuanian VAT
 * number: nine digits for legal entities or twelve for temporarily
 * registered taxpayers, with a fixed {@code 1} before the check digit and a
 * weighted modulo 11 check that retries with shifted weights on a
 * remainder of 10.
 */
public final class LtPvm implements StdNum {

    public static final LtPvm INSTANCE = new LtPvm();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("lt.pvm", "PVM")
                    .country("LT")
                    .title("Pridėtinės vertės mokestis mokėtojo kodas")
                    .description("Lithuanian VAT number: 9 or 12 digits with a weighted"
                            + " mod 11 check digit.")
                    .tags(Tag.VAT)
                    .build();

    private LtPvm() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        String n = Strings.compact(number, " -").toUpperCase(Locale.ROOT);
        return n.startsWith("LT") ? n.substring(2) : n;
    }

    /** The check digit for the base without its final digit. */
    public static char calcCheckDigit(String base) {
        int check = 0;
        for (int i = 0; i < base.length(); i++) {
            check += (1 + i % 9) * (base.charAt(i) - '0');
        }
        check %= 11;
        if (check == 10) {
            check = 0;
            for (int i = 0; i < base.length(); i++) {
                check += (1 + (i + 2) % 9) * (base.charAt(i) - '0');
            }
        }
        return (char) ('0' + check % 11 % 10);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.length() == 9) {
            if (n.charAt(7) != '1') {
                throw new InvalidComponentException("A 9-digit PVM has 1 before the check digit.");
            }
        } else if (n.length() == 12) {
            if (n.charAt(10) != '1') {
                throw new InvalidComponentException("A 12-digit PVM has 1 before the check digit.");
            }
        } else {
            throw new InvalidLengthException();
        }
        if (n.charAt(n.length() - 1) != calcCheckDigit(n.substring(0, n.length() - 1))) {
            throw new InvalidChecksumException();
        }
        return n;
    }
}
