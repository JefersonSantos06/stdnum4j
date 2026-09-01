package io.github.jefersonsantos06.stdnum.na;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidComponentException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

/**
 * PHN, the personal health number of British Columbia: ten digits opening
 * with a 9 and closing with a weighted mod 11 check digit.
 */
public final class CaBcPhn implements StdNum {

    public static final CaBcPhn INSTANCE = new CaBcPhn();

    private static final int[] WEIGHTS = {2, 4, 8, 5, 10, 9, 7, 3};

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("ca.bc_phn", "PHN")
                    .country("CA")
                    .title("British Columbia Personal Health Number")
                    .description("Health number of British Columbia: 10 digits starting with 9"
                            + " and ending in a weighted mod 11 check digit.")
                    .tags(Tag.PERSON, Tag.HEALTH)
                    .references("https://www2.gov.bc.ca/gov/content/health/"
                            + "health-drug-coverage/msp/bc-residents/get-a-personal-health-number")
                    .build();

    private CaBcPhn() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, "- ");
    }

    /** The check digit of a number, from the eight digits between the 9 and it. */
    public static char calcCheckDigit(String base) {
        int sum = 0;
        for (int i = 0; i < WEIGHTS.length && i < base.length(); i++) {
            sum += WEIGHTS[i] * (base.charAt(i) - '0') % 11;
        }
        return (char) ('0' + Math.floorMod(11 - sum, 11));
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 10) {
            throw new InvalidLengthException();
        }
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.charAt(0) != '9') {
            throw new InvalidComponentException("A health number starts with 9.");
        }
        if (n.charAt(9) != calcCheckDigit(n.substring(1, 9))) {
            throw new InvalidChecksumException();
        }
        return n;
    }

    @Override
    public String format(String number) {
        String n = validate(number);
        return n.substring(0, 4) + ' ' + n.substring(4, 7) + ' ' + n.substring(7);
    }
}
