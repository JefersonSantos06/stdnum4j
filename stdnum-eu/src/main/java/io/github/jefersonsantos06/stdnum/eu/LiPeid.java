package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

/**
 * PEID, the Liechtenstein identity number for people and legal entities:
 * four to twelve digits, with leading zeros conventionally omitted and
 * stripped here. The number carries no check digit.
 */
public final class LiPeid implements StdNum {

    public static final LiPeid INSTANCE = new LiPeid();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("li.peid", "PEID")
                    .country("LI")
                    .title("Liechtenstein PEID")
                    .description("Liechtenstein identity number: 4 to 12 digits with no check"
                            + " digit.")
                    .tags(Tag.COMPANY, Tag.PERSON)
                    .build();

    private LiPeid() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        String n = Strings.compact(number, " .");
        int i = 0;
        while (i < n.length() - 1 && n.charAt(i) == '0') {
            i++;
        }
        return n.substring(i);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() < 4 || n.length() > 12) {
            throw new InvalidLengthException();
        }
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        return n;
    }
}
