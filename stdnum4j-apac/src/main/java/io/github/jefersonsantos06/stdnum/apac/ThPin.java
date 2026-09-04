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

import java.util.List;

/**
 * PIN, the Thailand Personal Identification Number issued by the Ministry
 * of Interior: thirteen digits closed by a descending-weight mod 11 check
 * digit.
 */
public final class ThPin implements StdNum {

    public static final ThPin INSTANCE = new ThPin();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("th.pin", "PIN")
                    .country("TH")
                    .title("Thailand Personal Identification Number")
                    .description("Thai personal identification number: 13 digits with a"
                            + " weighted mod 11 check digit.")
                    .tags(Tag.PERSON)
                    .build();

    private static final Mask MASK = Mask.of("#-####-#####-##-#");

    private ThPin() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -");
    }

    /** The check digit for the twelve-digit base. */
    public static char calcCheckDigit(String base) {
        int sum = 0;
        for (int i = 0; i < 12 && i < base.length(); i++) {
            sum += (13 - i) * (base.charAt(i) - '0');
        }
        return (char) ('0' + Math.floorMod(11 - sum % 11, 10));
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
        if (n.charAt(0) == '0' || n.charAt(0) == '9') {
            // 0 marks a number issued by the Department of Business
            // Development (see ThMoa) and 9 is not assigned to people
            throw new InvalidComponentException(Message.of(ThPin.class, "pin.prefix",
                    "A personal identification number does not start with 0 or 9."));
        }
        if (n.charAt(12) != calcCheckDigit(n)) {
            throw new InvalidChecksumException();
        }
        return n;
    }

    @Override
    public String format(String number) {
        return MASK.fill(validate(number));
    }

    @Override
    public List<Mask> masks() {
        return List.of(MASK);
    }
}
