package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Mask;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.List;


/**
 * The Swedish postnummer: five digits, the first of which is never zero.
 */
public final class SePostnummer implements StdNum {

    public static final SePostnummer INSTANCE = new SePostnummer();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("se.postnummer", "Postnummer")
                    .country("SE")
                    .title("Svenskt postnummer")
                    .description("Swedish postcode: 5 digits, the first never zero.")
                    .tags(Tag.POSTAL)
                    .references("https://sv.wikipedia.org/wiki/Postnummer_i_Sverige")
                    .build();

    private static final Mask MASK = Mask.of("### ##");

    private SePostnummer() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -", "SE");
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n) || n.charAt(0) == '0') {
            throw new InvalidFormatException();
        }
        if (n.length() != 5) {
            throw new InvalidLengthException();
        }
        return n;
    }

    @Override
    public String format(String number) {
        return MASK.fill(validate(number));
    }

    @Override
    public List<Mask> masks() {
        return List.of(MASK);
    }
}
