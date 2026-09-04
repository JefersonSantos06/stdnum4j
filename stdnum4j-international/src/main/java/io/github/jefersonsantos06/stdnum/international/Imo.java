package io.github.jefersonsantos06.stdnum.international;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;


/**
 * IMO number (International Maritime Organization), identifying a ship's
 * hull: six sequential digits and a descending-weight mod 10 check digit,
 * usually written with the {@code IMO} prefix.
 */
public final class Imo implements StdNum {

    public static final Imo INSTANCE = new Imo();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("imo", "IMO")
                    .title("IMO ship identification number")
                    .description("Ship hull identifier: 6 digits and a weighted mod 10"
                            + " check digit.")
                    .tags(Tag.VEHICLE)
                    .references("https://en.wikipedia.org/wiki/IMO_number")
                    .build();

    private Imo() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -", "IMO");
    }

    /** The check digit for the six-digit base. */
    public static char calcCheckDigit(String base) {
        int sum = 0;
        for (int i = 0; i < 6 && i < base.length(); i++) {
            sum += (base.charAt(i) - '0') * (7 - i);
        }
        return (char) ('0' + sum % 10);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.length() != 7) {
            throw new InvalidLengthException();
        }
        if (n.charAt(6) != calcCheckDigit(n)) {
            throw new InvalidChecksumException();
        }
        return n;
    }

    @Override
    public String format(String number) {
        return "IMO " + validate(number);
    }
}
