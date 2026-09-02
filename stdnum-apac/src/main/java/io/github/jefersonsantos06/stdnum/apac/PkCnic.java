package io.github.jefersonsantos06.stdnum.apac;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.Message;
import io.github.jefersonsantos06.stdnum.spi.Reasons;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Map;
import java.util.Optional;

/**
 * CNIC (Computerised National Identity Card number), the Pakistani
 * identity number: thirteen digits — a province digit, a locality, a
 * family number and a final digit that is odd for males and even for
 * females. There is no check digit.
 */
public final class PkCnic implements StdNum {

    public static final PkCnic INSTANCE = new PkCnic();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("pk.cnic", "CNIC")
                    .country("PK")
                    .title("Computerised National Identity Card number")
                    .description("Pakistani identity number: 13 digits encoding province and"
                            + " gender, with no check digit.")
                    .tags(Tag.PERSON)
                    .build();

    private static final Map<Character, String> PROVINCES = Map.of(
            '1', "Khyber Pakhtunkhwa",
            '2', "FATA",
            '3', "Punjab",
            '4', "Sindh",
            '5', "Balochistan",
            '6', "Islamabad",
            '7', "Gilgit-Baltistan");

    private PkCnic() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, "-");
    }

    /** The gender encoded in the final digit: {@code 'M'} or {@code 'F'}. */
    public static char getGender(String number) {
        String n = INSTANCE.validate(number);
        return (n.charAt(12) - '0') % 2 == 0 ? 'F' : 'M';
    }

    /** The province the number was issued in. */
    public static Optional<String> getProvince(String number) {
        return Optional.ofNullable(PROVINCES.get(INSTANCE.validate(number).charAt(0)));
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.length() != 13) {
            throw new InvalidLengthException();
        }
        if (n.charAt(12) == '0') {
            throw new InvalidComponentException(Message.of(PkCnic.class, "cnic.gender",
                    "The gender digit must not be zero."));
        }
        if (!PROVINCES.containsKey(n.charAt(0))) {
            throw new InvalidComponentException(Reasons.unknownProvince());
        }
        return n;
    }

    @Override
    public String format(String number) {
        String n = validate(number);
        return n.substring(0, 5) + "-" + n.substring(5, 12) + "-" + n.substring(12);
    }
}
