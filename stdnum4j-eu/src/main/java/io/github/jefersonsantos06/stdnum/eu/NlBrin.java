package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * BRIN, the number of a Dutch school: two digits and two letters, optionally
 * followed by two digits naming one of the school's locations.
 */
public final class NlBrin implements StdNum {

    public static final NlBrin INSTANCE = new NlBrin();

    private static final Pattern PATTERN = Pattern.compile("[0-9]{2}[A-Z]{2}([0-9]{2})?");

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("nl.brin", "BRIN")
                    .country("NL")
                    .title("Basisregistratie Instellingen")
                    .description("Number of a Dutch school: 2 digits and 2 letters, with an"
                            + " optional 2-digit location.")
                    .tags(Tag.EDUCATION)
                    .references("https://nl.wikipedia.org/wiki/BRIN-nummer")
                    .build();

    private NlBrin() {
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
        if (n.length() != 4 && n.length() != 6) {
            throw new InvalidLengthException();
        }
        if (!PATTERN.matcher(n).matches()) {
            throw new InvalidFormatException();
        }
        return n;
    }
}
