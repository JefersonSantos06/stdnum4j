package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

/**
 * EIC, the code that identifies a party, an area or a metering point on the
 * European energy market: sixteen characters, the last a check character.
 *
 * <p>The dash is a filler inside the code rather than a separator, so it is
 * not stripped — though the code may not end on one, the last position being
 * the check character.</p>
 */
public final class EuEic implements StdNum {

    public static final EuEic INSTANCE = new EuEic();

    private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-";

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("eu.eic", "EIC")
                    .title("European Energy Identification Code")
                    .description("Identifier of a party, area or metering point on the"
                            + " European energy market: 16 characters with a check character.")
                    .tags(Tag.OTHER)
                    .references("https://www.entsoe.eu/data/energy-identification-codes-eic/")
                    .build();

    private EuEic() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " ");
    }

    /** The check character of a code, from its first fifteen characters. */
    public static char calcCheckDigit(String number) {
        String n = INSTANCE.compact(number);
        int sum = 0;
        for (int i = 0; i < 15 && i < n.length(); i++) {
            int value = ALPHABET.indexOf(n.charAt(i));
            if (value < 0) {
                throw new InvalidFormatException();
            }
            sum += (16 - i) * value;
        }
        return ALPHABET.charAt(36 - Math.floorMod(sum - 1, 37));
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        for (int i = 0; i < n.length(); i++) {
            if (ALPHABET.indexOf(n.charAt(i)) < 0) {
                throw new InvalidFormatException();
            }
        }
        if (n.length() != 16) {
            throw new InvalidLengthException();
        }
        if (n.charAt(15) == '-') {
            throw new InvalidFormatException(Message.of(EuEic.class, "eic.check-filler",
                    "The check character cannot be a filler."));
        }
        if (n.charAt(15) != calcCheckDigit(n)) {
            throw new InvalidChecksumException();
        }
        return n;
    }
}
