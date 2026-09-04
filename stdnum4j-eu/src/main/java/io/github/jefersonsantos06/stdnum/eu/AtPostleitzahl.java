package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.numdb.NumDb;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Map;

/**
 * The Austrian postcode: four digits with no check digit, so what makes one
 * valid is being a code that is actually in use.
 *
 * <p>Only the codes an address can carry are accepted. The regulator also
 * lists post office box and internal codes, which no letter is addressed
 * to.</p>
 */
public final class AtPostleitzahl implements StdNum {

    public static final AtPostleitzahl INSTANCE = new AtPostleitzahl();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("at.postleitzahl", "Postleitzahl")
                    .country("AT")
                    .title("Osterreichische Postleitzahl")
                    .description("Austrian postcode: 4 digits, checked against the codes in"
                            + " use rather than by a checksum.")
                    .tags(Tag.POSTAL)
                    .references("https://www.rtr.at/TKP/aktuelles/veroeffentlichungen/veroeffentlichungen/"
                            + "Postleitzahlen.de.html")
                    .build();

    private AtPostleitzahl() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    /** The postcodes in use, with the place and Bundesland of each. */
    private static NumDb codes() {
        return NumDb.load(AtPostleitzahl.class, "at-postleitzahl.dat");
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, "");
    }

    /**
     * The place and the Bundesland the code belongs to, or an empty map if it
     * is not one in use.
     */
    public static Map<String, String> info(String number) {
        return codes().info(INSTANCE.compact(number)).get(0).properties();
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.length() != 4) {
            throw new InvalidLengthException();
        }
        if (info(n).isEmpty()) {
            throw new InvalidComponentException(Message.of(AtPostleitzahl.class, "postleitzahl.in-use",
                    "Not a postcode in use."));
        }
        return n;
    }
}
