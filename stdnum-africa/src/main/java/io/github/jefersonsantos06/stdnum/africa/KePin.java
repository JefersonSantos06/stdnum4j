package io.github.jefersonsantos06.stdnum.africa;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * KRA PIN, the Kenyan personal identification number for tax purposes:
 * {@code A} for individuals or {@code P} for non-individuals, nine digits
 * and a trailing letter. The number carries no check digit.
 */
public final class KePin implements StdNum {

    public static final KePin INSTANCE = new KePin();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("ke.pin", "KRA PIN")
                    .country("KE")
                    .title("Kenya Revenue Authority Personal Identification Number")
                    .description("Kenyan tax number: A or P, 9 digits and a letter, with no"
                            + " check digit.")
                    .tags(Tag.TAX)
                    .build();

    private static final Pattern STRUCTURE = Pattern.compile("[AP][0-9]{9}[A-Z]");

    private KePin() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -").toUpperCase(Locale.ROOT);
    }

    /** Whether the number belongs to an individual rather than an entity. */
    public static boolean isIndividual(String number) {
        return INSTANCE.validate(number).charAt(0) == 'A';
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 11) {
            throw new InvalidLengthException();
        }
        if (!STRUCTURE.matcher(n).matches()) {
            throw new InvalidFormatException();
        }
        return n;
    }
}
