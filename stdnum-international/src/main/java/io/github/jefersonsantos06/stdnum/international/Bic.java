package io.github.jefersonsantos06.stdnum.international;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * BIC (Business Identifier Code, ISO 9362), also known as the SWIFT code.
 *
 * <p>Eight or eleven characters: a four-letter business party prefix, a
 * two-letter ISO country code, a two-character alphanumeric suffix and an
 * optional three-character branch code. Validation is structural; there is
 * no check digit in a BIC.</p>
 */
public final class Bic implements StdNum {

    public static final Bic INSTANCE = new Bic();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("bic", "BIC")
                    .title("Business Identifier Code")
                    .description("ISO 9362 bank/business identifier (SWIFT code): structural"
                            + " validation of the 8 or 11 character form.")
                    .tags(Tag.BANK)
                    .references("https://en.wikipedia.org/wiki/ISO_9362")
                    .build();

    private static final Pattern STRUCTURE =
            Pattern.compile("[A-Z]{4}[A-Z]{2}[A-Z0-9]{2}(?:[A-Z0-9]{3})?");

    private Bic() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -").toUpperCase(Locale.ROOT);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 8 && n.length() != 11) {
            throw new InvalidLengthException();
        }
        if (!STRUCTURE.matcher(n).matches()) {
            throw new InvalidFormatException();
        }
        return n;
    }

    /** The ISO country code embedded in the BIC. */
    public static String countryCode(String number) {
        return INSTANCE.validate(number).substring(4, 6);
    }

    /** The branch code, present only in the 11-character form. */
    public static Optional<String> branchCode(String number) {
        String n = INSTANCE.validate(number);
        return n.length() == 11 ? Optional.of(n.substring(8)) : Optional.empty();
    }
}
