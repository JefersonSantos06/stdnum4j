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
 * VID, the revocable virtual identifier a resident can generate in place of
 * their {@link InAadhaar} number: sixteen digits with a Verhoeff check digit.
 * As with the Aadhaar it stands for, a palindrome is not a valid number.
 */
public final class InVid implements StdNum {

    public static final InVid INSTANCE = new InVid();

    private static final Pattern PATTERN = Pattern.compile("[2-9][0-9]{15}");

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("in.vid", "VID")
                    .country("IN")
                    .title("Indian Virtual ID")
                    .description("Revocable stand-in for an Aadhaar number: 16 digits with a"
                            + " Verhoeff check digit, never a palindrome.")
                    .tags(Tag.PERSON)
                    .references("https://uidai.gov.in/en/contact-support/have-any-question/"
                            + "284-faqs/aadhaar-online-services/virtual-id-vid.html")
                    .build();

    private InVid() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -");
    }

    /** Written in four groups of four, as the Aadhaar it stands in for is. */
    @Override
    public String format(String number) {
        String n = validate(number);
        return n.substring(0, 4) + " " + n.substring(4, 8) + " "
                + n.substring(8, 12) + " " + n.substring(12);
    }

    /**
     * The number with everything but its last four digits struck out, which
     * is how the Ministry of Electronics and Information Technology has it
     * shown to anyone who does not need the whole of it.
     */
    public static String mask(String number) {
        return "XXXX XXXX XXXX " + INSTANCE.validate(number).substring(12);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 16) {
            throw new InvalidLengthException();
        }
        if (!PATTERN.matcher(n).matches()) {
            throw new InvalidFormatException();
        }
        if (n.contentEquals(new StringBuilder(n).reverse())) {
            throw new InvalidFormatException(Message.of(InVid.class, "vid.palindrome",
                    "A virtual ID cannot be a palindrome."));
        }
        Verhoeff.validate(n);
        return n;
    }
}
