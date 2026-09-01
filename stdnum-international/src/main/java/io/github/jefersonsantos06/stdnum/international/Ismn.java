package io.github.jefersonsantos06.stdnum.international;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;

/**
 * ISMN (International Standard Music Number), identifying sheet music:
 * either the legacy ten-character form starting with {@code M}, or the
 * thirteen-digit form in the {@code 9790} bookland prefix. Both are
 * validated with the EAN check digit.
 */
public final class Ismn implements StdNum {

    public static final Ismn INSTANCE = new Ismn();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("ismn", "ISMN")
                    .title("International Standard Music Number")
                    .description("Sheet music identifier in its legacy 10-character and"
                            + " 13-digit forms, with the EAN check digit.")
                    .tags(Tag.MEDIA)
                    .references("https://en.wikipedia.org/wiki/International_Standard_Music_Number")
                    .build();

    private Ismn() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -.").toUpperCase(Locale.ROOT);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() == 10) {
            if (n.charAt(0) != 'M') {
                throw new InvalidFormatException("A 10-character ISMN starts with M.");
            }
            Ean.INSTANCE.validate("9790" + n.substring(1));
        } else if (n.length() == 13) {
            if (!n.startsWith("9790")) {
                throw new InvalidComponentException("A 13-digit ISMN starts with 9790.");
            }
            Ean.INSTANCE.validate(n);
        } else {
            throw new InvalidLengthException();
        }
        return n;
    }

    /** Validates and returns the number in its 13-digit form. */
    public static String convertTo13(String number) {
        String n = INSTANCE.validate(number);
        return n.length() == 13 ? n : "9790" + n.substring(1);
    }
}
