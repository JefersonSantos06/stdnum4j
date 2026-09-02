package io.github.jefersonsantos06.stdnum.apac;

import io.github.jefersonsantos06.stdnum.algo.Verhoeff;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.regex.Pattern;

/**
 * Aadhaar, the Indian personal identity number issued by the UIDAI: twelve
 * digits not starting with 0 or 1, closed by a Verhoeff check digit.
 */
public final class InAadhaar implements StdNum {

    public static final InAadhaar INSTANCE = new InAadhaar();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("in.aadhaar", "Aadhaar")
                    .country("IN")
                    .title("Aadhaar")
                    .description("Indian personal identity number: 12 digits with a Verhoeff"
                            + " check digit.")
                    .tags(Tag.PERSON)
                    .build();

    private static final Pattern STRUCTURE = Pattern.compile("[2-9][0-9]{11}");

    private InAadhaar() {
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
        if (n.length() != 12) {
            throw new InvalidLengthException();
        }
        if (!STRUCTURE.matcher(n).matches()) {
            throw new InvalidFormatException();
        }
        if (n.contentEquals(new StringBuilder(n).reverse())) {
            throw new InvalidFormatException(Message.of(InAadhaar.class, "aadhaar.palindrome",
                    "An Aadhaar cannot be a palindrome."));
        }
        Verhoeff.validate(n);
        return n;
    }

    @Override
    public String format(String number) {
        String n = validate(number);
        return n.substring(0, 4) + " " + n.substring(4, 8) + " " + n.substring(8);
    }

    /** The number with everything but the last four digits masked. */
    public static String mask(String number) {
        return "XXXX XXXX " + INSTANCE.validate(number).substring(8);
    }
}
