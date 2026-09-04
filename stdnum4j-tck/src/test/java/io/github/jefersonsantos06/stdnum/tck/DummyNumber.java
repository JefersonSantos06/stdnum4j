package io.github.jefersonsantos06.stdnum.tck;

import io.github.jefersonsantos06.stdnum.algo.Luhn;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

/**
 * A synthetic number used to exercise the TCK itself: 8 digits, the last one
 * a Luhn check digit, formatted {@code "1234-5674"}. Also serves as the
 * reference example of the implementation pattern real modules follow.
 */
public final class DummyNumber implements StdNum {

    public static final DummyNumber INSTANCE = new DummyNumber();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("zz.dummy", "DUMMY")
                    .country("ZZ")
                    .title("Synthetic TCK example number")
                    .description("Eight digits ending in a Luhn check digit.")
                    .tags(Tag.OTHER)
                    .build();

    private DummyNumber() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -.");
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
        Luhn.validate(n);
        return n;
    }

    @Override
    public String format(String number) {
        String n = validate(number);
        return n.substring(0, 4) + "-" + n.substring(4);
    }
}
