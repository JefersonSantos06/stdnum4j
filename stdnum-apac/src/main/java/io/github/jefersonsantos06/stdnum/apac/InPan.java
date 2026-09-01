package io.github.jefersonsantos06.stdnum.apac;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * PAN (Permanent Account Number), the Indian income tax identifier: five
 * letters, four digits and a check letter. The fourth character gives the
 * type of holder; the check letter uses an undocumented algorithm and is
 * therefore not verified.
 */
public final class InPan implements StdNum {

    public static final InPan INSTANCE = new InPan();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("in.pan", "PAN")
                    .country("IN")
                    .title("Permanent Account Number")
                    .description("Indian income tax identifier: 5 letters, 4 digits and a"
                            + " check letter (the check algorithm is undocumented).")
                    .tags(Tag.TAX, Tag.PERSON, Tag.COMPANY)
                    .build();

    private static final Pattern STRUCTURE = Pattern.compile("[A-Z]{5}[0-9]{4}[A-Z]");

    /** The holder type encoded in the fourth character. */
    private static final Map<Character, String> HOLDER_TYPES = Map.ofEntries(
            Map.entry('A', "Association of Persons (AOP)"),
            Map.entry('B', "Body of Individuals (BOI)"),
            Map.entry('C', "Company"),
            Map.entry('F', "Firm/Limited Liability Partnership"),
            Map.entry('G', "Government Agency"),
            Map.entry('H', "Hindu Undivided Family (HUF)"),
            Map.entry('J', "Artificial Juridical Person"),
            Map.entry('K', "Krish (Trust Krish)"),
            Map.entry('L', "Local Authority"),
            Map.entry('P', "Individual"),
            Map.entry('T', "Trust"));

    private InPan() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -").toUpperCase(Locale.ROOT);
    }

    /** The kind of holder the number was issued to. */
    public static Optional<String> holderType(String number) {
        return Optional.ofNullable(HOLDER_TYPES.get(INSTANCE.validate(number).charAt(3)));
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 10) {
            throw new InvalidLengthException();
        }
        if (!STRUCTURE.matcher(n).matches()) {
            throw new InvalidFormatException();
        }
        if (!HOLDER_TYPES.containsKey(n.charAt(3))) {
            throw new InvalidComponentException("Unknown holder type.");
        }
        if (n.startsWith("0000", 5)) {
            throw new InvalidComponentException("The serial number must not be zero.");
        }
        return n;
    }

    /**
     * The number with the four serial digits masked, following the CBDT
     * masking standard: {@code AAPPV8261K} becomes {@code AAPPVXXXXK}.
     */
    public static String mask(String number) {
        String n = INSTANCE.validate(number);
        return n.substring(0, 5) + "XXXX" + n.substring(9);
    }

    /** The initial of the holder's name or surname, in the fifth position. */
    public static char initial(String number) {
        return INSTANCE.validate(number).charAt(4);
    }

}
