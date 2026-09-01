package io.github.jefersonsantos06.stdnum.apac;

import io.github.jefersonsantos06.stdnum.algo.Luhn;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

/**
 * Mispar Zehut (מספר זהות), the Israeli identity number issued at birth:
 * nine Luhn-checked digits, commonly written without their leading zeros,
 * which are restored here.
 */
public final class IlIdnr implements StdNum {

    public static final IlIdnr INSTANCE = new IlIdnr();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("il.idnr", "Mispar Zehut")
                    .country("IL")
                    .title("Israeli identity number")
                    .description("Israeli identity number: 9 digits with a Luhn check digit.")
                    .tags(Tag.PERSON)
                    .build();

    private IlIdnr() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        String n = Strings.compact(number, " -");
        return n.length() < 9 && Strings.isDigits(n) ? "0".repeat(9 - n.length()) + n : n;
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.length() != 9) {
            throw new InvalidLengthException();
        }
        if (Long.parseLong(n) <= 0) {
            throw new InvalidFormatException("The number must be greater than zero.");
        }
        Luhn.validate(n);
        return n;
    }

    @Override
    public String format(String number) {
        String n = validate(number);
        return n.substring(0, 8) + "-" + n.substring(8);
    }

}
