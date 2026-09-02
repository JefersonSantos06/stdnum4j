package io.github.jefersonsantos06.stdnum.africa;

import io.github.jefersonsantos06.stdnum.algo.Luhn;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;

/**
 * TIN (South African Tax Identification Number), also called the tax
 * reference number: ten Luhn-checked digits starting with 0, 1, 2, 3 or 9.
 */
public final class ZaTin implements StdNum {

    public static final ZaTin INSTANCE = new ZaTin();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("za.tin", "TIN")
                    .country("ZA")
                    .title("South African Tax Identification Number")
                    .description("South African tax reference number: 10 digits with a Luhn"
                            + " check digit.")
                    .tags(Tag.TAX)
                    .build();

    private ZaTin() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -/").toUpperCase(Locale.ROOT);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 10) {
            throw new InvalidLengthException();
        }
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if ("01239".indexOf(n.charAt(0)) < 0) {
            throw new InvalidComponentException(Message.of(ZaTin.class, "tin.prefix",
                    "A TIN starts with 0, 1, 2, 3 or 9."));
        }
        Luhn.validate(n);
        return n;
    }
}
