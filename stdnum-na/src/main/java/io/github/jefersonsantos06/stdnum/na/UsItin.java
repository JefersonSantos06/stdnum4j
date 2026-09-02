package io.github.jefersonsantos06.stdnum.na;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.regex.Pattern;

/**
 * ITIN (U.S. Individual Taxpayer Identification Number), issued to people
 * who need a taxpayer number but cannot get an SSN: nine digits starting
 * with 9, whose fourth and fifth digits fall in 70-99 excluding 89 and 93.
 */
public final class UsItin implements StdNum {

    public static final UsItin INSTANCE = new UsItin();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("us.itin", "ITIN")
                    .country("US")
                    .title("Individual Taxpayer Identification Number")
                    .description("US taxpayer number for people without an SSN: 9 digits"
                            + " starting with 9 and a restricted group range.")
                    .tags(Tag.PERSON, Tag.TAX)
                    .build();

    /** The separators, if written, sit after the area and the group. */
    private static final Pattern STRUCTURE = Pattern.compile("[0-9]{3}-?[0-9]{2}-?[0-9]{4}");

    private UsItin() {
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
        if (!STRUCTURE.matcher(Strings.compact(number, "")).matches()) {
            throw new InvalidFormatException();
        }
        String n = compact(number);
        if (!Strings.isDigits(n) || n.length() != 9) {
            throw new InvalidFormatException();
        }
        if (n.charAt(0) != '9') {
            throw new InvalidComponentException(Message.of(UsItin.class, "itin.prefix",
                    "An ITIN starts with 9."));
        }
        int group = Integer.parseInt(n.substring(3, 5));
        if (group < 70 || group > 99 || group == 89 || group == 93) {
            throw new InvalidComponentException(Message.of(UsItin.class, "itin.group",
                    "This ITIN group range is not issued."));
        }
        return n;
    }

    @Override
    public String format(String number) {
        String n = validate(number);
        return n.substring(0, 3) + "-" + n.substring(3, 5) + "-" + n.substring(5);
    }
}
