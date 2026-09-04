package io.github.jefersonsantos06.stdnum.na;

import io.github.jefersonsantos06.stdnum.algo.Luhn;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Mask;
import io.github.jefersonsantos06.stdnum.text.Strings;

/**
 * SIN (Canadian Social Insurance Number): nine digits validated with the
 * Luhn checksum. Numbers starting with 0 or 8 are not issued; a leading 9
 * marks temporary workers.
 */
public final class CaSin implements StdNum {

    public static final CaSin INSTANCE = new CaSin();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("ca.sin", "SIN")
                    .country("CA")
                    .title("Social Insurance Number")
                    .description("Canadian social insurance number: 9 digits with a Luhn"
                            + " checksum.")
                    .tags(Tag.PERSON)
                    .build();

    private static final Mask MASK = Mask.of("###-###-###");

    private CaSin() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, "- ");
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 9) {
            throw new InvalidLengthException();
        }
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.charAt(0) == '0' || n.charAt(0) == '8') {
            throw new InvalidComponentException(Message.of(CaSin.class, "sin.prefix",
                    "SINs do not start with 0 or 8."));
        }
        Luhn.validate(n);
        return n;
    }

    @Override
    public String format(String number) {
        return MASK.fill(validate(number));
    }
}
