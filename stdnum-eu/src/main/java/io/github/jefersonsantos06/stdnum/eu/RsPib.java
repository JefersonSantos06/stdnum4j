package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.algo.Iso7064;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

/**
 * PIB (Порески идентификациони број), the Serbian tax number: nine digits
 * closed by an ISO 7064 MOD 11,10 check digit.
 */
public final class RsPib implements StdNum {

    public static final RsPib INSTANCE = new RsPib();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("rs.pib", "PIB")
                    .country("RS")
                    .title("Порески идентификациони број")
                    .description("Serbian tax number: 9 digits with an ISO 7064 MOD 11,10"
                            + " check digit.")
                    .tags(Tag.TAX, Tag.VAT)
                    .build();

    private RsPib() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -.");
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.length() != 9) {
            throw new InvalidLengthException();
        }
        Iso7064.MOD_11_10.validate(n);
        return n;
    }
}
