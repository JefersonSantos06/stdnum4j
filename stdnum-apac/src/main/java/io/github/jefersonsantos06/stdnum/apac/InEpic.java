package io.github.jefersonsantos06.stdnum.apac;

import io.github.jefersonsantos06.stdnum.algo.Luhn;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * EPIC, the number on the Indian voter identity card: a three-letter code for
 * the issuing constituency followed by seven digits, the last of which is a
 * Luhn check digit over the digits alone.
 */
public final class InEpic implements StdNum {

    public static final InEpic INSTANCE = new InEpic();

    private static final Pattern PATTERN = Pattern.compile("[A-Z]{3}[0-9]{7}");

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("in.epic", "EPIC")
                    .country("IN")
                    .title("Electoral Photo Identity Card number")
                    .description("Indian voter ID number: a 3-letter constituency code and 7"
                            + " digits ending in a Luhn check digit.")
                    .tags(Tag.PERSON)
                    .references("https://en.wikipedia.org/wiki/Voter_ID_(India)")
                    .build();

    private InEpic() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -").toUpperCase(Locale.ROOT);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 10) {
            throw new InvalidLengthException();
        }
        if (!PATTERN.matcher(n).matches()) {
            throw new InvalidFormatException();
        }
        Luhn.validate(n.substring(3));
        return n;
    }
}
