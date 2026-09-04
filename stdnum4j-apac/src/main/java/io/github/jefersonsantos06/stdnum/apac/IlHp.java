package io.github.jefersonsantos06.stdnum.apac;

import io.github.jefersonsantos06.stdnum.algo.Luhn;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

/**
 * H.P., the Israeli company number: nine digits opening with a 5 and closing
 * with a Luhn check digit. It is the company counterpart of the personal
 * {@link IlIdnr}, which shares the length and the checksum.
 */
public final class IlHp implements StdNum {

    public static final IlHp INSTANCE = new IlHp();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("il.hp", "H.P.")
                    .country("IL")
                    .title("Israeli company number")
                    .description("Israeli company number: 9 digits starting with 5 and ending"
                            + " in a Luhn check digit.")
                    .tags(Tag.COMPANY)
                    .references("https://www.oecd.org/content/dam/oecd/en/topics/policy-issue-focus/aeoi/israel-tin.pdf")
                    .build();

    private IlHp() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -");
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
        if (n.charAt(0) != '5') {
            throw new InvalidComponentException(Message.of(IlHp.class, "hp.prefix",
                    "Israeli company numbers start with 5."));
        }
        Luhn.validate(n);
        return n;
    }
}
