package io.github.jefersonsantos06.stdnum.apac;

import io.github.jefersonsantos06.stdnum.spi.Descriptor;
import io.github.jefersonsantos06.stdnum.spi.InvalidChecksumException;
import io.github.jefersonsantos06.stdnum.spi.InvalidFormatException;
import io.github.jefersonsantos06.stdnum.spi.InvalidLengthException;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.Tag;
import io.github.jefersonsantos06.stdnum.text.Strings;

/**
 * TFN (Australian Tax File Number), issued by the ATO to taxpayers: eight
 * digits (older numbers) or nine, weighted 1,4,3,7,5,8,6,9,10 modulo 11.
 */
public final class AuTfn implements StdNum {

    public static final AuTfn INSTANCE = new AuTfn();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("au.tfn", "TFN")
                    .country("AU")
                    .title("Tax File Number")
                    .description("Australian tax file number: 8 or 9 digits with a weighted"
                            + " mod 11 checksum.")
                    .tags(Tag.PERSON, Tag.TAX)
                    .build();

    private static final int[] WEIGHTS = {1, 4, 3, 7, 5, 8, 6, 9, 10};

    private AuTfn() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " ");
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.length() != 8 && n.length() != 9) {
            throw new InvalidLengthException();
        }
        int sum = 0;
        for (int i = 0; i < n.length(); i++) {
            sum += WEIGHTS[i] * (n.charAt(i) - '0');
        }
        if (sum % 11 != 0) {
            throw new InvalidChecksumException();
        }
        return n;
    }

    @Override
    public String format(String number) {
        String n = validate(number);
        return n.length() == 9
                ? n.substring(0, 3) + " " + n.substring(3, 6) + " " + n.substring(6)
                : n.substring(0, 2) + " " + n.substring(2, 5) + " " + n.substring(5);
    }
}
