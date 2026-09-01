package io.github.jefersonsantos06.stdnum.apac;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

/**
 * UBN (統一編號), the Taiwanese unified business number: eight digits whose
 * weighted digit sum must be a multiple of five. Numbers whose seventh
 * digit is 7 have a second chance, since that position may contribute an
 * extra unit.
 */
public final class TwUbn implements StdNum {

    public static final TwUbn INSTANCE = new TwUbn();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("tw.ubn", "UBN")
                    .country("TW")
                    .title("Unified Business Number (統一編號)")
                    .description("Taiwanese business number: 8 digits whose weighted digit"
                            + " sum is a multiple of five.")
                    .tags(Tag.COMPANY, Tag.TAX, Tag.VAT)
                    .build();

    private static final int[] WEIGHTS = {1, 2, 1, 2, 1, 2, 4, 1};

    private TwUbn() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -");
    }

    /** The weighted digit sum modulo five; valid numbers yield 0. */
    public static int checksum(String number) {
        String n = INSTANCE.compact(number);
        int sum = 0;
        for (int i = 0; i < WEIGHTS.length && i < n.length(); i++) {
            int product = WEIGHTS[i] * (n.charAt(i) - '0');
            while (product > 0) {
                sum += product % 10;
                product /= 10;
            }
        }
        return sum % 5;
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (n.length() != 8) {
            throw new InvalidLengthException();
        }
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        int checksum = checksum(n);
        // a 7 in the seventh position may contribute an extra unit
        if (checksum != 0 && !(checksum == 4 && n.charAt(6) == '7')) {
            throw new InvalidChecksumException();
        }
        return n;
    }
}
