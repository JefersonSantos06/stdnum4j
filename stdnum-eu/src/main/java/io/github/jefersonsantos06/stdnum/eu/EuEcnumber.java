package io.github.jefersonsantos06.stdnum.eu;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

import java.util.regex.Pattern;

/**
 * The EC number, which the European Community gives a chemical substance:
 * seven digits written in three groups, the last being a check digit.
 *
 * <p>The dashes are part of how the number is written, so a number given
 * without them has them put back when it is compacted.</p>
 */
public final class EuEcnumber implements StdNum {

    public static final EuEcnumber INSTANCE = new EuEcnumber();

    private static final Pattern PATTERN = Pattern.compile("[0-9]{3}-[0-9]{3}-[0-9]");

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("eu.ecnumber", "EC number")
                    .title("European Community number")
                    .description("Identifier of a chemical substance in the European"
                            + " Community inventories: 7 digits with a check digit.")
                    .tags(Tag.PRODUCT)
                    .references("https://echa.europa.eu/information-on-chemicals")
                    .build();

    private EuEcnumber() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        String n = Strings.compact(number, " ");
        if (n.indexOf('-') >= 0) {
            return n;
        }
        // written without its dashes: put them back where they belong
        return slice(n, 0, 3) + '-' + slice(n, 3, 6) + '-' + slice(n, 6, n.length());
    }

    /** The characters between two positions, clamped to what is there. */
    private static String slice(String n, int from, int to) {
        int start = Math.min(from, n.length());
        return n.substring(start, Math.max(start, Math.min(to, n.length())));
    }

    /**
     * The check digit of a number, from the six digits before it. A weighted
     * sum leaving a remainder of 10 has no check digit, and the reference
     * takes the first character of it, which this mirrors.
     */
    public static char calcCheckDigit(String number) {
        String n = INSTANCE.compact(number).replace("-", "");
        int sum = 0;
        for (int i = 0; i < n.length(); i++) {
            int digit = n.charAt(i) - '0';
            if (digit < 0 || digit > 9) {
                throw new InvalidFormatException();
            }
            sum += (i + 1) * digit;
        }
        return Integer.toString(sum % 11).charAt(0);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 9) {
            throw new InvalidLengthException();
        }
        if (!PATTERN.matcher(n).matches()) {
            throw new InvalidFormatException();
        }
        if (n.charAt(8) != calcCheckDigit(n.substring(0, 8))) {
            throw new InvalidChecksumException();
        }
        return n;
    }
}
