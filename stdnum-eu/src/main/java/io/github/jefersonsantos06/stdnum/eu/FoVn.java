package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;

/**
 * V-number (Vinnutal), the Faroe Islands tax number: six digits with no
 * check digit.
 */
public final class FoVn implements StdNum {

    public static final FoVn INSTANCE = new FoVn();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("fo.vn", "V-number")
                    .country("FO")
                    .title("Vinnutal")
                    .description("Faroe Islands tax number: 6 digits with no check digit.")
                    .tags(Tag.TAX, Tag.VAT)
                    .build();

    private FoVn() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        String n = Strings.compact(number, " -.").toUpperCase(Locale.ROOT);
        return n.startsWith("FO") ? n.substring(2) : n;
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 6) {
            throw new InvalidLengthException();
        }
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        return n;
    }
}
