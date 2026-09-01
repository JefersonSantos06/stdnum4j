package io.github.jefersonsantos06.stdnum.na;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

/**
 * EIN (U.S. Employer Identification Number), also called FEIN: a two-digit
 * campus prefix and a seven-digit serial. There is no check digit.
 */
public final class UsEin implements StdNum {

    public static final UsEin INSTANCE = new UsEin();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("us.ein", "EIN")
                    .country("US")
                    .title("Employer Identification Number")
                    .description("US business tax identifier: 9 digits with no check digit.")
                    .tags(Tag.COMPANY, Tag.TAX)
                    .build();

    private UsEin() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, "-");
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n) || n.length() != 9) {
            throw new InvalidFormatException();
        }
        return n;
    }

    @Override
    public String format(String number) {
        String n = validate(number);
        return n.substring(0, 2) + "-" + n.substring(2);
    }
}
