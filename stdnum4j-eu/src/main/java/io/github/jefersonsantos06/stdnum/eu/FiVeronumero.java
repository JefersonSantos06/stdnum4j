package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

/**
 * Veronumero, the tax number on the identity card everyone working on a
 * Finnish construction site must wear: twelve digits with no check digit.
 * It stands in for the {@link FiHetu}, which the card no longer shows.
 */
public final class FiVeronumero implements StdNum {

    public static final FiVeronumero INSTANCE = new FiVeronumero();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("fi.veronumero", "Veronumero")
                    .country("FI")
                    .title("Suomalainen veronumero")
                    .description("Finnish individual tax number for construction workers:"
                            + " 12 digits.")
                    .tags(Tag.PERSON, Tag.TAX)
                    .references("https://www.vero.fi/en/individuals/tax-cards-and-tax-returns/"
                            + "arriving_in_finland/work_in_finland/"
                            + "specific-instructions-for-different-occupations/"
                            + "coming-to-a-construction-site-or-a-shipyard/Tax_number/")
                    .build();

    private FiVeronumero() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " ");
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.length() != 12) {
            throw new InvalidLengthException();
        }
        return n;
    }
}
