package io.github.jefersonsantos06.stdnum.africa;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Mask;
import io.github.jefersonsantos06.stdnum.text.Strings;

/**
 * TN (الرقم الضريبي), the Egyptian tax registration number: nine digits,
 * conventionally written in three hyphenated groups. Arabic-Indic digits
 * are accepted and normalised to ASCII. The number carries no check digit.
 */
public final class EgTn implements StdNum {

    public static final EgTn INSTANCE = new EgTn();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("eg.tn", "TN")
                    .country("EG")
                    .title("Egyptian Tax Registration Number")
                    .description("Egyptian tax number: 9 digits with no check digit;"
                            + " Arabic-Indic digits are accepted.")
                    .tags(Tag.TAX, Tag.VAT)
                    .build();

    private static final Mask MASK = Mask.of("###-###-###");

    private EgTn() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -/");
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
        return n;
    }

    @Override
    public String format(String number) {
        return MASK.fill(validate(number));
    }
}
