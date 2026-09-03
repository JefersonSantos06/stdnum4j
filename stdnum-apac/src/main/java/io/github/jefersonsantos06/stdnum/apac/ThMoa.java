package io.github.jefersonsantos06.stdnum.apac;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Mask;
import io.github.jefersonsantos06.stdnum.text.Strings;

/**
 * MOA (Memorandum of Association Number), the Thai company taxpayer number
 * issued by the Department of Business Development: the same thirteen
 * digits and check digit as {@link ThPin}, but always starting with zero
 * to mark a DBD-issued number, and grouped differently.
 */
public final class ThMoa implements StdNum {

    public static final ThMoa INSTANCE = new ThMoa();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("th.moa", "MOA")
                    .country("TH")
                    .title("Memorandum of Association Number")
                    .description("Thai company taxpayer number: 13 digits starting with 0,"
                            + " with the same check digit as the personal number.")
                    .tags(Tag.COMPANY, Tag.TAX, Tag.VAT)
                    .build();

    private static final Mask MASK = Mask.of("#-##-#-###-#####-#");

    private ThMoa() {
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
        if (n.length() != 13) {
            throw new InvalidLengthException();
        }
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.charAt(0) != '0') {
            throw new InvalidComponentException(Message.of(ThMoa.class, "moa.dbd-prefix",
                    "A DBD-issued number starts with 0."));
        }
        if (n.charAt(12) != ThPin.calcCheckDigit(n)) {
            throw new InvalidChecksumException();
        }
        return n;
    }

    @Override
    public String format(String number) {
        return MASK.fill(validate(number));
    }
}
