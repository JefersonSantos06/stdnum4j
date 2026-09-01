package io.github.jefersonsantos06.stdnum.na;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

/**
 * RTN (Routing Transit Number), the nine-digit identifier used by the US
 * banking system to route deposits between banks. The last digit is an
 * ABA check digit weighted 7,3,9 repeating.
 */
public final class UsRtn implements StdNum {

    public static final UsRtn INSTANCE = new UsRtn();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("us.rtn", "RTN")
                    .country("US")
                    .title("Routing Transit Number")
                    .description("US bank routing number: 9 digits with the ABA weighted"
                            + " mod 10 check digit.")
                    .tags(Tag.BANK)
                    .build();

    private static final int[] WEIGHTS = {7, 3, 9, 7, 3, 9, 7, 3};

    private UsRtn() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -");
    }

    /** The check digit for the eight-digit base. */
    public static char calcCheckDigit(String base) {
        int sum = 0;
        for (int i = 0; i < WEIGHTS.length && i < base.length(); i++) {
            sum += WEIGHTS[i] * (base.charAt(i) - '0');
        }
        return (char) ('0' + sum % 10);
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.length() != 9) {
            throw new InvalidLengthException();
        }
        if (n.charAt(8) != calcCheckDigit(n.substring(0, 8))) {
            throw new InvalidChecksumException();
        }
        return n;
    }
}
