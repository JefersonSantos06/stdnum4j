package io.github.jefersonsantos06.stdnum.apac;

import io.github.jefersonsantos06.stdnum.algo.Verhoeff;
import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
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
            throw new InvalidFormatException("A virtual ID cannot be a palindrome.");
        }
        Verhoeff.validate(n);
        return n;
    }
}
