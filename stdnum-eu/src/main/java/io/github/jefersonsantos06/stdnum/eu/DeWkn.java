package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;

/**
 * WKN, the German securities identification number: six characters drawn
 * from the digits and the letters, less I and O, which would be read as 1
 * and 0. It carries no check digit; the ISIN built from it does.
 */
public final class DeWkn implements StdNum {

    public static final DeWkn INSTANCE = new DeWkn();

    /** The digits and the letters bar I and O. */
    private static final String ALPHABET = "0123456789ABCDEFGHJKLMNPQRSTUVWXYZ";

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("de.wkn", "WKN")
                    .country("DE")
                    .title("Wertpapierkennnummer")
                    .description("German securities identification number: 6 characters from"
                            + " the digits and the letters bar I and O.")
                    .tags(Tag.FINANCIAL)
                    .references("https://de.wikipedia.org/wiki/Wertpapierkennnummer")
                    .build();

    private DeWkn() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " ").toUpperCase(Locale.ROOT);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        for (int i = 0; i < n.length(); i++) {
            if (ALPHABET.indexOf(n.charAt(i)) < 0) {
                throw new InvalidFormatException();
            }
        }
        if (n.length() != 6) {
            throw new InvalidLengthException();
        }
        return n;
    }
}
